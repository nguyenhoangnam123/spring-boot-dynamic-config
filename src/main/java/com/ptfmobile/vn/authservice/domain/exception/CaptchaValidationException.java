package com.ptfmobile.vn.authservice.domain.exception;

public class CaptchaValidationException extends Exception{

  public CaptchaValidationException(String errorMessage) {
    super(errorMessage);
  }
}

