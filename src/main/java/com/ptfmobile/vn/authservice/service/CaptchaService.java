package com.ptfmobile.vn.authservice.service;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.ptfmobile.vn.authservice.config.captcha.GoogleResponse;
import com.ptfmobile.vn.authservice.domain.dto.OtpDto;
import com.ptfmobile.vn.authservice.domain.dto.OtpOutput;
import com.ptfmobile.vn.authservice.domain.exception.CaptchaValidationException;
import com.ptfmobile.vn.authservice.domain.exception.RequestException;
import com.ptfmobile.vn.authservice.domain.request.GetOtpRequest;
import com.ptfmobile.vn.common.DBConstant.TableName;
import com.ptfmobile.vn.mongodb.BaseMongoDao;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class CaptchaService {
    Logger logger = LoggerFactory.getLogger(CaptchaService.class);
    private static final String CHANGE_PASSWORD = "changePassword";
    private final BaseMongoDao baseMongoDao;
    private final RestTemplate restTemplate;

    @Value("${google.recaptcha.endpoint}")
    private String GOOGLE_RECAPTCHA_ENDPOINT;

    @Autowired
    public CaptchaService(MongoDatabase mongoDatabase, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.baseMongoDao = new BaseMongoDao(mongoDatabase);
    }

    @Value("${google.recaptcha.web.key.secret}")
    private String recaptchaSecretWeb;

    @Value("${google.recaptcha.mobile.key.secret}")
    private String recaptchaSecretMobile;

    public OtpOutput getOtpForChangePassword(GetOtpRequest request)
            throws CaptchaValidationException, RequestException {
        if (!CHANGE_PASSWORD.equalsIgnoreCase(request.getType())) {
            throw new RequestException("Request type không hợp lệ");
        }
        GoogleResponse captchaVerification = getCaptchaValidationResponse(request);
        if (!captchaVerification.isSuccess()) {
            logger.error("Captcha verification fail! Error: {}", captchaVerification.getErrorCodes()[0]);
            throw new CaptchaValidationException("Xác thực captcha không thành công");
        }
        // kiem tra email da ton tai otp chua expire
        List<Bson> params = new ArrayList<>();
        Bson otpEmailExist = Filters.eq("email", request.getEmail());
        Bson otpExpireTime = Filters.gt("expireTime", new Date());
        params.add(otpExpireTime);
        params.add(otpEmailExist);
        Bson paramsFilter = Filters.and(params);

        OtpDto otpsExist = baseMongoDao.findOneV2(TableName.OTPS, paramsFilter, OtpDto.class);

        return Objects.nonNull(otpsExist)
                ? updateExistingOtpDocument(paramsFilter, otpsExist)
                : createNewOtpDocument(request);
    }

    private OtpOutput createNewOtpDocument(GetOtpRequest request) {
        String otp;
        otp = new DecimalFormat("000000").format(new Random().nextInt(999999));
        //check duplicate
        Bson otpFilter = Filters.eq("verificationCode", otp);
        while (baseMongoDao.isExist(TableName.OTPS, otpFilter)) {
            otp = new DecimalFormat("000000").format(new Random().nextInt(999999));
        }

        OtpDto otpDto = new OtpDto();
        otpDto.setEmail(request.getEmail());
        otpDto.setType(request.getType());
        otpDto.setVerificationCode(otp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 15);
        otpDto.setExpireTime(calendar.getTime());

        baseMongoDao.insertOne(TableName.OTPS, otpDto.toDoc());
        OtpOutput output = new OtpOutput();
        output.setOtp(otp);
        output.setExpireTime(calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        return output;
    }

    private OtpOutput updateExistingOtpDocument(Bson paramsFilter, OtpDto otpsExist) {
        String otp;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 15);
        Document updateValue = new Document("$set", new Document("expireTime", calendar.getTime()));
        otp = otpsExist.getVerificationCode();
        baseMongoDao.update(TableName.OTPS, paramsFilter, updateValue);
        OtpOutput output = new OtpOutput();
        output.setOtp(otp);
        output.setExpireTime(calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        return output;
    }

    private GoogleResponse getCaptchaValidationResponse(GetOtpRequest request)
            throws CaptchaValidationException {
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        if ("android".equalsIgnoreCase(request.getTypeCaptcha())) {
            requestMap.add("secret", recaptchaSecretMobile);
        } else {
            requestMap.add("secret", recaptchaSecretWeb);
        }
        requestMap.add("response", request.getCaptchaToken());
        try {
            return restTemplate.postForObject(GOOGLE_RECAPTCHA_ENDPOINT, requestMap, GoogleResponse.class);
        } catch (Exception e) {
            logger.error("Không thể kết nối tới Google recaptcha! Kiểm tra lại cài đặt.");
            throw new CaptchaValidationException("Không thể kết nối tới Google recaptcha! Kiểm tra lại cài đặt.");
        }


    }
}