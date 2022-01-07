package com.ptfmobile.vn.authservice.domain.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptfmobile.vn.common.AppUtils;
import com.ptfmobile.vn.db.dto.BaseDTO;
import java.io.IOException;
import java.util.Date;
import lombok.Data;
import org.bson.Document;

@Data
public class OtpDto extends BaseDTO {
  private String type;
  private String email;
  private String verificationCode;
  private Date expireTime;

  public void fromDoc(Document document) {
    setType(AppUtils.parseString(document.get("type")));
    setEmail(AppUtils.parseString(document.get("email")));
    setVerificationCode(AppUtils.parseString(document.get("verificationCode")));
    setExpireTime(document.getDate("expireTime"));

  }

  public Document toDoc() {
    try {
      String json;
      ObjectMapper objectMapper = new ObjectMapper();
      json = objectMapper.writeValueAsString(this);
      Document document = Document.parse(json);
      document.put("expireTime", expireTime);
      document.remove("id");
      document.remove("hashKey");
      return document;
    } catch (IOException ex) {
      return null;
    }
  }

  @Override
  public String validate() {
    return null;
  }
}
