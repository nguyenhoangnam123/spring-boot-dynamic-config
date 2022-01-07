package com.ptfmobile.vn.authservice.domain.response;

import com.ptfmobile.vn.cache.cacheDTO.UserCache;

public class AuthenticationResponse extends UserCache {

  private String tokenUser;

  public String getTokenUser() {
    return tokenUser;
  }

  public void setTokenUser(String tokenUser) {
    this.tokenUser = tokenUser;
  }
}
