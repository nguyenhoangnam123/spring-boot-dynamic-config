package com.ptfmobile.vn.authservice.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

@Data
public class OtpOutput {

  private String otp;
  private LocalDateTime expireTime;
}
