package com.ptfmobile.vn.authservice.jwt;


import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.ptfmobile.vn.authservice.domain.exception.LogoutException;
import com.ptfmobile.vn.authservice.domain.exception.UserNotExistException;
import com.ptfmobile.vn.authservice.domain.request.AuthenticationRequest;
import com.ptfmobile.vn.authservice.domain.response.AuthenticationResponse;
import com.ptfmobile.vn.cache.MyCache;
import com.ptfmobile.vn.cache.cacheDTO.UserCache;
import com.ptfmobile.vn.common.BaseItemResponse;
import com.ptfmobile.vn.common.BaseResponse;
import com.ptfmobile.vn.common.DBConstant.TableName;
import com.ptfmobile.vn.db.dto.BaseDTO;
import com.ptfmobile.vn.db.dto.UserToken;
import com.ptfmobile.vn.mongodb.BaseMongoDao;
import io.jsonwebtoken.*;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;

import static java.util.stream.Collectors.joining;

@Component
public class JwtTokenProvider {

    private final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String AUTHORITIES_KEY = "roles";
    private static final String HEADER_PREFIX = "Bearer";

    private final JwtProperties jwtProperties;
    private final MyCache myCache;
    private final BaseMongoDao baseMongoDao;

    private String secretKey;

    @Autowired
    public JwtTokenProvider(JwtProperties jwtProperties,
                            RedissonClient redissonClient,
                            MongoDatabase mongoDatabase) {
        this.jwtProperties = jwtProperties;
        this.myCache = new MyCache(redissonClient, mongoDatabase);
        this.baseMongoDao = new BaseMongoDao(mongoDatabase);
    }

    @PostConstruct
    public void init() {
        secretKey = Base64.getEncoder().encodeToString(jwtProperties.getSecretKey().getBytes());
    }

    public BaseResponse createAndSaveToken(Authentication authentication, AuthenticationRequest request) throws UserNotExistException {
        BaseDTO userData = myCache.getData(TableName.USERS, authentication.getName(), "userId", com.ptfmobile.vn.db.dto.User.class);
        if (Objects.isNull(userData)) {
            throw new UserNotExistException("Tài khoản không tồn tại");
        }

        String jwtString = createToken(authentication);
        saveToken(jwtString, authentication, request);

        extractedResponse(authentication, jwtString, userData);
        return extractedResponse(authentication, jwtString, userData);
    }

    private void saveToken(String jwtString, Authentication authentication, AuthenticationRequest request) {
        UserToken userToken = new UserToken();
        userToken.setToken(jwtString);
        userToken.setUserId(authentication.getName());
        userToken.setFirebaseToken(request.getFirebaseToken());
        logger.info("save token to db: {} with userName: {}", jwtString, authentication.getName());
        baseMongoDao.insertOne(TableName.USER_TOKENS, userToken.toDoc());
        //save to cache
        logger.info("save token to cache: {} with userName: {}", jwtString, authentication.getName());
        myCache.setData(TableName.USER_TOKENS, jwtString, authentication.getName());
    }


    private BaseResponse extractedResponse(Authentication authentication, String jwtString, BaseDTO user) {
        if (user instanceof UserCache) {
            UserCache userData = (UserCache) user;
            BaseItemResponse<AuthenticationResponse> response = new BaseItemResponse<>();
            AuthenticationResponse payload = new AuthenticationResponse();
            payload.setGroupId(userData.getGroupId());
            payload.setRoleId(userData.getRoleId());
            payload.setUserId(authentication.getName());
            payload.setEmail(userData.getEmail());
            payload.setSaleId(userData.getSaleId());
            payload.setTokenUser(jwtString);
            payload.setIsDelete(userData.getIsDelete());
            payload.setIsSuperAdmin(userData.getIsSuperAdmin());
            payload.setStatus(userData.getStatus());
            response.setSuccess(payload);
            return response;
        } else {
            com.ptfmobile.vn.db.dto.User userData = (com.ptfmobile.vn.db.dto.User) user;
            BaseItemResponse<AuthenticationResponse> response = new BaseItemResponse<>();
            AuthenticationResponse payload = new AuthenticationResponse();
            payload.setGroupId(userData.getGroupId());
            payload.setRoleId(userData.getRoleId());
            payload.setUserId(authentication.getName());
            payload.setEmail(userData.getEmail());
            payload.setSaleId(userData.getSaleId());
            payload.setTokenUser(jwtString);
            payload.setIsDelete(userData.getIsDelete());
            payload.setIsSuperAdmin(userData.getIsSuperAdmin());
            payload.setStatus(userData.getStatus());
            response.setSuccess(payload);
            return response;
        }
    }

    private String createToken(Authentication authentication) {

        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Claims claims = Jwts.claims().setSubject(username);
        if (!authorities.isEmpty()) {
            claims.put(AUTHORITIES_KEY,
                    authorities.stream().map(GrantedAuthority::getAuthority).collect(joining(",")));
        }
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, this.secretKey)
                .compact();

    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();

        Object authoritiesClaim = claims.get(AUTHORITIES_KEY);

        Collection<? extends GrantedAuthority> authorities =
                authoritiesClaim == null ? AuthorityUtils.NO_AUTHORITIES
                        : AuthorityUtils.commaSeparatedStringToAuthorityList(authoritiesClaim.toString());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return Objects.nonNull(claims);
            //Check with expire date
            //return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String getTokenFromHeader(Map<String, String> headers) {
        String bearerToken = headers.get(HttpHeaders.AUTHORIZATION.toLowerCase(Locale.ROOT));
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(HEADER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public BaseResponse deleteTokenAndLogout(Map<String, String> headers) throws LogoutException {
        BaseResponse response = new BaseResponse();
        String token = getTokenFromHeader(headers);
        if (Objects.nonNull(token) && StringUtils.hasText(token)) {

            logger.info("Delete token in cache...");
            long tokenExistInDb = baseMongoDao.deleteOne(TableName.USER_TOKENS, Filters.eq("token", token));
            if (tokenExistInDb != 0) {
                response.setSuccess(true);
            }
            logger.info("Delete token in db...");
            boolean tokenExistInCache = myCache.isKeyExist(TableName.USER_TOKENS, token);
            if (tokenExistInCache) {
                myCache.deleteObjByKey(TableName.USER_TOKENS, token);
                response.setSuccess(true);
            }

            if (response.isSuccess()) return response;
        }
        throw new LogoutException("Token không tồn tại");
    }
}