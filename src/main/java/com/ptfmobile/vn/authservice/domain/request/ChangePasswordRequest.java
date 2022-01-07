package com.ptfmobile.vn.authservice.domain.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordRequest {

  @NotEmpty(message = "Mã OTP không được để trống")
  private String verifyCode;

  @NotEmpty(message = "Email không được để trống")
  @Email(message = "Email không đúng định dạng")
  private String email;
  @Pattern(regexp = "^(?=.*[\\W])(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[#$^+=!*()@%&~-]).{6,18}$", message = "Mật khẩu tối thiểu 6 ký tự và tối đa 18 kí tự, có chứa số, ký tự đặc biệt, chữ viết hoa và chữ viết thường.")
  private String password;
}
