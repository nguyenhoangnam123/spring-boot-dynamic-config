package com.ptfmobile.vn.authservice.domain.exception;

public class UserNotExistException extends Exception {

  public UserNotExistException(String errorMessage) {
    super(errorMessage);
  }
}

