package com.ptfmobile.vn.authservice.service;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.ptfmobile.vn.authservice.client.NotifyFeignClient;
import com.ptfmobile.vn.authservice.domain.dto.OtpDto;
import com.ptfmobile.vn.authservice.domain.dto.OtpOutput;
import com.ptfmobile.vn.authservice.domain.exception.CaptchaValidationException;
import com.ptfmobile.vn.authservice.domain.exception.RequestException;
import com.ptfmobile.vn.authservice.domain.request.ChangePasswordRequest;
import com.ptfmobile.vn.authservice.domain.request.GetOtpRequest;
import com.ptfmobile.vn.cache.MyCache;
import com.ptfmobile.vn.common.BaseResponse;
import com.ptfmobile.vn.common.DBConstant.TableName;
import com.ptfmobile.vn.common.message.SendEmailPayload;
import com.ptfmobile.vn.db.dto.User;
import com.ptfmobile.vn.db.dto.UserToken;
import com.ptfmobile.vn.mongodb.BaseMongoDao;
import org.apache.commons.codec.digest.DigestUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PasswordService {
  private final Logger logger = LoggerFactory.getLogger(PasswordService.class);

  @Value("${salt.user-password}")
  private String userPasswordSalt;

  private final NotifyFeignClient notifyFeignClient;
  private final CaptchaService captchaService;
  private final BaseMongoDao baseMongoDao;
  private final MyCache myCache;

  @Autowired
  public PasswordService(NotifyFeignClient notifyFeignClient, CaptchaService captchaService, MongoDatabase mongoDatabase, RedissonClient redissonClient){
    this.baseMongoDao = new BaseMongoDao(mongoDatabase);
    this.captchaService = captchaService;
    this.notifyFeignClient = notifyFeignClient;
    this.myCache = new MyCache(redissonClient, mongoDatabase);
  }

  public void verifyOtpChangePassword(GetOtpRequest request)
      throws CaptchaValidationException, RequestException {
    OtpOutput otpOutput = captchaService.getOtpForChangePassword(request);
    SendEmailPayload payload = new SendEmailPayload();
    BeanUtils.copyProperties(request, payload);
    payload.setOtp(otpOutput.getOtp());
    payload.setExpireTime(otpOutput.getExpireTime());
    logger.info("Send email payload to queue: {}", payload);
    notifyFeignClient.sendEmail(payload);
  }

  public BaseResponse changePassword(ChangePasswordRequest request) throws RequestException {
    BaseResponse response = new BaseResponse();
    verifyotp(request);

    Bson condition = Filters.eq("email", request.getEmail());
    String password = generatePasswordHashed(userPasswordSalt + request.getPassword());
    Document updateValue = new Document();
    updateValue.put("password", password);
    baseMongoDao.update(TableName.USERS, condition,
        new Document("$set", updateValue));

    logger.info("Update password for email: {} success", request.getEmail());
    //remove all otp
    Bson otpCodeFilter = Filters.eq("verificationCode", request.getVerifyCode());
    long numberOtpDeleted = baseMongoDao.deleteMany(TableName.OTPS, otpCodeFilter);
    logger.info("Number otp deleted: {}", numberOtpDeleted);

    //remove all token
    deleteTokenInDbAndCache(request.getEmail());
    response.setSuccess(true);
    return response;
  }

  private void deleteTokenInDbAndCache(String email) {
    Bson condition = Filters.eq("email", email);
    User user = baseMongoDao.findOneV2(TableName.USERS, condition, User.class);
    if(Objects.nonNull(user)){
      Bson conditionUserId = Filters.eq("userId", user.getUserId());
      List<UserToken> userTokens = baseMongoDao.find(TableName.USER_TOKENS, conditionUserId, UserToken.class);
      Set<String> tokens = userTokens.stream()
          .map(UserToken::getToken)
          .collect(Collectors.toSet());
      long numberTokenDeleted = baseMongoDao.deleteMany(TableName.USER_TOKENS, conditionUserId);
      logger.info("Number token record deleted in db: {}", numberTokenDeleted);

      logger.info("Delete all token of email {} in cache", email);
      myCache.deleteObjByListKey(TableName.USER_TOKENS, tokens);
    }

  }

  private String generatePasswordHashed(String originalPassword) {
    return DigestUtils.sha256Hex(originalPassword);
  }

  private void verifyotp(ChangePasswordRequest request) throws RequestException {
    List<Bson> params = new ArrayList<>();
    Bson otpEmailExist = Filters.eq("email", request.getEmail());
    Bson otpExpireTime = Filters.gt("expireTime", new Date());
    Bson otpCode = Filters.eq("verificationCode", request.getVerifyCode());
    params.add(otpExpireTime);
    params.add(otpEmailExist);
    params.add(otpCode);
    Bson paramsFilter = Filters.and(params);
    OtpDto otp = baseMongoDao.findOneV2(TableName.OTPS, paramsFilter, OtpDto.class);
    if(!Objects.nonNull(otp)){
      logger.error("OTP không hợp lệ hoặc không trùng khớp với email");
      throw new RequestException("OTP không hợp lệ hoặc không trùng khớp với email");
    }
  }
}
