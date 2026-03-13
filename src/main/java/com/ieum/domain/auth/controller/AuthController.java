package com.ieum.domain.auth.controller;

import com.ieum.domain.auth.dto.SendSmsRequest;
import com.ieum.domain.auth.dto.SignupRequest;
import com.ieum.domain.auth.dto.VerifySmsRequest;
import com.ieum.domain.auth.dto.VerifySmsResponse;
import com.ieum.domain.auth.service.AuthService;
import com.ieum.domain.auth.service.SmsService;
import com.ieum.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SmsService smsService;
    private final AuthService authService;

    @PostMapping("/sms/send")
    public ApiResponse<Void> sendSms(@RequestBody @Valid SendSmsRequest request) {
        smsService.sendCode(request.phone());
        return ApiResponse.ok();
    }

    @PostMapping("/sms/verify")
    public ApiResponse<VerifySmsResponse> verifySms(@RequestBody @Valid VerifySmsRequest request) {
        String verifiedToken = smsService.verifyCode(request.phone(), request.code());
        return ApiResponse.ok(new VerifySmsResponse(verifiedToken));
    }

    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody @Valid SignupRequest request) {
        authService.signup(request);
        return ApiResponse.ok();
    }
}
