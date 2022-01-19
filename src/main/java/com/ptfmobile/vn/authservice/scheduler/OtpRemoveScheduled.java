package com.ptfmobile.vn.authservice.scheduler;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.ptfmobile.vn.common.DBConstant.TableName;
import com.ptfmobile.vn.mongodb.BaseMongoDao;
import java.util.Date;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OtpRemoveScheduled {

  private final BaseMongoDao baseMongoDao;

  @Autowired
  public OtpRemoveScheduled(MongoDatabase mongoDatabase){
    this.baseMongoDao = new BaseMongoDao(mongoDatabase);
  }
  Logger logger = LoggerFactory.getLogger(OtpRemoveScheduled.class);

  @Scheduled(cron = "${cron.expression}")
  public void deleteOtpExpireDaily() {
    Bson expireOtpCondition = Filters.lt("expireTime", new Date());
    logger.info("Schedule: Delete all expired OTP ---------->");
    long numberOtpDeleted = baseMongoDao.deleteMany(TableName.OTPS, expireOtpCondition);
    logger.info("Number otp deleted: {}" , numberOtpDeleted);
  }
}
