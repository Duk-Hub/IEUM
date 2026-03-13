package com.ieum.domain.auth.service;

import com.ieum.global.exception.CustomException;
import com.ieum.global.exception.ErrorCode;
import com.ieum.infra.redis.RedisKeys;
import com.ieum.infra.redis.RedisService;
import com.ieum.infra.sms.SmsSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SmsService {

    private static final Duration THROTTLE_TTL = Duration.ofMinutes(1);
    private static final Duration CODE_TTL = Duration.ofMinutes(3);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(10);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RedisService redisService;
    private final SmsSender smsSender;

    public void sendCode(String phone) {
        if (redisService.hasKey(RedisKeys.smsThrottle(phone))) {
            throw new CustomException(ErrorCode.SMS_SEND_TOO_MANY_REQUESTS);
        }

        String code = generateCode();
        redisService.set(RedisKeys.smsThrottle(phone), "", THROTTLE_TTL);
        redisService.set(RedisKeys.smsCode(phone), code, CODE_TTL);
        smsSender.send(phone, "[이음] 인증번호 [" + code + "]를 입력해주세요.");
    }

    public String verifyCode(String phone, String code) {
        String saved = redisService.get(RedisKeys.smsCode(phone));

        if (saved == null) {
            throw new CustomException(ErrorCode.SMS_CODE_EXPIRED);
        }
        if (!saved.equals(code)) {
            throw new CustomException(ErrorCode.SMS_CODE_MISMATCH);
        }

        redisService.delete(RedisKeys.smsCode(phone));

        String verifiedToken = UUID.randomUUID().toString();
        redisService.set(RedisKeys.smsVerified(verifiedToken), phone, VERIFIED_TTL);

        return verifiedToken;
    }

    private String generateCode() {
        int code = SECURE_RANDOM.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
