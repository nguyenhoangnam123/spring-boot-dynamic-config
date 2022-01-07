package com.ptfmobile.vn.authservice.domain.exception;

import com.ptfmobile.vn.common.BaseResponse;
import com.ptfmobile.vn.common.ErrorCodeDefs;
import com.ptfmobile.vn.common.ErrorDetail;
import com.ptfmobile.vn.common.ErrorResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

/**
 * @author HuyNV
 */
@RestControllerAdvice
public class CustomExceptionHandler {

  @ExceptionHandler(UserNotExistException.class)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public BaseResponse handleUserNotFoundException(UserNotExistException ex) {
    BaseResponse response = new BaseResponse();
    response.setFailed(ErrorCodeDefs.ERR_OBJECT_NOT_FOUND, ex.getMessage());
    return response;
  }

  @ExceptionHandler(UserUnavailableException.class)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public BaseResponse handleUserUnavailableException(UserUnavailableException ex) {
    BaseResponse response = new BaseResponse();
    response.setFailed(ErrorCodeDefs.ERR_ACCOUNT_LOCKED, ex.getMessage());
    return response;
  }

  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public BaseResponse handleBadCredentialException(BadCredentialsException ex) {
    BaseResponse response = new BaseResponse();
    ErrorDetail error = new ErrorDetail();

    error.setMessage(ex.getMessage());
    response.setFailed(ErrorCodeDefs.ERR_UNAUTHORIZED, ErrorCodeDefs.getMessage(ErrorCodeDefs.ERR_UNAUTHORIZED));
    return response;
  }


  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public BaseResponse handleInvalidJwtException(AuthenticationException ex) {
    BaseResponse response = new BaseResponse();
    response.setFailed(ErrorCodeDefs.ERR_UNAUTHORIZED, ErrorCodeDefs.getMessage(ErrorCodeDefs.ERR_UNAUTHORIZED));
    return response;
  }

  @ExceptionHandler(LogoutException.class)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public BaseResponse handleLogoutException(LogoutException ex) {
    BaseResponse response = new BaseResponse();
    response.setFailed(ErrorCodeDefs.ERR_UNAUTHORIZED, ex.getMessage());
    return response;
  }

  @ExceptionHandler(WebExchangeBindException.class)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public BaseResponse handleException(WebExchangeBindException ex) {
    BaseResponse response = new BaseResponse();
    List<FieldError> errors = ex.getBindingResult().getFieldErrors();
    List<ErrorDetail> errorDetails = new ArrayList<>();
    for (FieldError fieldError : errors) {
      ErrorDetail error = new ErrorDetail();
      error.setId(fieldError.getField());
      error.setMessage(fieldError.getDefaultMessage());
      errorDetails.add(error);
    }

    ErrorResponse errorResponse = new ErrorResponse();
    errorResponse.setErrors(errorDetails);
    errorResponse.setCode(ErrorCodeDefs.ERR_VALIDATION);
    errorResponse.setMessage(ErrorCodeDefs.getMessage(ErrorCodeDefs.ERR_VALIDATION));
    response.setSuccess(false);
    response.setError(errorResponse);
    return response;
  }

  @ExceptionHandler(CaptchaValidationException.class)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public BaseResponse captchaValidationException(CaptchaValidationException ex) {
    BaseResponse response = new BaseResponse();
    response.setFailed(ErrorCodeDefs.ERR_BAD_REQUEST, ex.getMessage());
    return response;
  }

  @ExceptionHandler(RequestException.class)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public BaseResponse requestTypeException(RequestException ex) {
    BaseResponse response = new BaseResponse();
    response.setFailed(ErrorCodeDefs.ERR_BAD_REQUEST, ex.getMessage());
    return response;
  }
}
