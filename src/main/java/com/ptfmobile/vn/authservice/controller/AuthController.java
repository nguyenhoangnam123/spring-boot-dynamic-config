package com.ptfmobile.vn.authservice.controller;

import com.ptfmobile.vn.authservice.domain.exception.CaptchaValidationException;
import com.ptfmobile.vn.authservice.domain.exception.LogoutException;
import com.ptfmobile.vn.authservice.domain.exception.RequestException;
import com.ptfmobile.vn.authservice.domain.exception.UserNotExistException;
import com.ptfmobile.vn.authservice.domain.request.AuthenticationRequest;
import com.ptfmobile.vn.authservice.domain.request.ChangePasswordRequest;
import com.ptfmobile.vn.authservice.domain.request.GetOtpRequest;
import com.ptfmobile.vn.authservice.jwt.JwtTokenProvider;
import com.ptfmobile.vn.authservice.service.PasswordService;
import com.ptfmobile.vn.common.BaseResponse;
import com.ptfmobile.vn.common.ErrorCodeDefs;
import com.ptfmobile.vn.common.MethodType;
import com.ptfmobile.vn.common.ModuleApiType;
import com.ptfmobile.vn.common.annotation.ModuleDescriptionAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private ReactiveAuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordService passwordService;

    @PostMapping("/login")
    @ModuleDescriptionAPI(
            name = "Đăng nhập",
            module = "auth",
            path = "/auth/login",
            description = "Đăng nhập hệ thống",
            type = ModuleApiType.PUBLIC,
            adminMetadata = false,
            method = MethodType.POST)
    public Mono<BaseResponse> login(@RequestBody AuthenticationRequest request) {
        Mono<Authentication> authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        return authentication.map(auth -> {
            try {
                return jwtTokenProvider.createAndSaveToken(auth, request);
            } catch (UserNotExistException e) {
                BaseResponse response = new BaseResponse();
                response.setFailed(ErrorCodeDefs.ERR_OBJECT_NOT_FOUND, ErrorCodeDefs.getMessage(ErrorCodeDefs.ERR_OBJECT_NOT_FOUND));
                return response;
            }
        });
    }

    @GetMapping("/logout")
    @ModuleDescriptionAPI(
            name = "Đăng xuất",
            module = "auth",
            path = "/auth/logout",
            description = "Đăng xuất hệ thống",
            type = ModuleApiType.PUBLIC,
            adminMetadata = false,
            method = MethodType.GET)
    public BaseResponse logout(@RequestHeader Map<String, String> headers) throws LogoutException {
        return jwtTokenProvider.deleteTokenAndLogout(headers);
    }


    @PostMapping(value = "/get-otp")
    @ModuleDescriptionAPI(
        name = "Gửi OTP change password",
        module = "auth",
        path = "/auth/get-otp",
        description = "Gửi OTP change password",
        type = ModuleApiType.PUBLIC,
        adminMetadata = false,
        method = MethodType.POST)
    public BaseResponse getOtpChangePassword(@Valid @RequestBody GetOtpRequest request)
        throws IOException, CaptchaValidationException, RequestException {
        BaseResponse response = new BaseResponse();
        passwordService.verifyOtpChangePassword(request);
        response.setSuccess(true);
        return response;
    }

    @PostMapping(value = "/change-password")
    @ModuleDescriptionAPI(
        name = "Thay đổi mật khẩu qua OTP",
        module = "auth",
        path = "/auth/change-password",
        description = "Thay đổi mật khẩu qua OTP",
        type = ModuleApiType.PUBLIC,
        adminMetadata = false,
        method = MethodType.POST)
    public BaseResponse changePasswordByOtp(@Valid @RequestBody ChangePasswordRequest request)
        throws RequestException {
        return passwordService.changePassword(request);
    }
}
