package com.ptfmobile.vn.authservice.domain.exception;

public class UserUnavailableException extends Exception {

  public UserUnavailableException(String errorMessage) {
    super(errorMessage);
  }
}
