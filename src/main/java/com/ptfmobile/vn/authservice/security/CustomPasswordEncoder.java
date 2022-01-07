package com.ptfmobile.vn.authservice.security;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class CustomPasswordEncoder implements PasswordEncoder {

  @Value("${salt.user-password}")
  private String passwordSalt;

  @Override
  public String encode(CharSequence plainTextPassword) {
    return DigestUtils.sha256Hex(passwordSalt + plainTextPassword.toString());
  }
  @Override
  public boolean matches(CharSequence plainTextPassword, String passwordInDatabase) {
    return passwordInDatabase.equals(DigestUtils.sha256Hex(passwordSalt + plainTextPassword.toString()));
  }
}
