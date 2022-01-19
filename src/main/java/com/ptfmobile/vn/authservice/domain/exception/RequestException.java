package com.ptfmobile.vn.authservice.domain.exception;

public class RequestException extends Exception{
  public RequestException(String errorMessage) {
    super(errorMessage);
  }
}
