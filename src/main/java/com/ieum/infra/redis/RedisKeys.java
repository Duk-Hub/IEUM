package com.ieum.infra.redis;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisKeys {

    public static String smsCode(String phone) {
        return "auth:sms:code:" + phone;
    }

    public static String smsThrottle(String phone) {
        return "auth:sms:throttle:" + phone;
    }

    public static String smsVerified(String token) {
        return "auth:sms:verified:" + token;
    }

    public static String refreshToken(Long memberId) {
        return "auth:refresh:" + memberId;
    }

    public static String blacklist(String accessToken) {
        return "auth:blacklist:" + accessToken;
    }
}
