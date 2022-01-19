package com.ptfmobile.vn.authservice.domain.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class GetOtpRequest {

  @NotEmpty(message = "Type không được để trống")
  private String type;

  @NotEmpty(message = "Email không được để trống")
  @Email(message = "Email không đúng định dạng")
  private String email;
  private String captchaToken;
  private String typeCaptcha;
}
