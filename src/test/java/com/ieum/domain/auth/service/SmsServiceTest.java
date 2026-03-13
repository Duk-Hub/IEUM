package com.ieum.domain.auth.service;

import com.ieum.global.exception.CustomException;
import com.ieum.global.exception.ErrorCode;
import com.ieum.infra.redis.RedisKeys;
import com.ieum.infra.redis.RedisService;
import com.ieum.infra.sms.SmsSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("SmsService")
class SmsServiceTest {

    private static final String PHONE = "01012345678";
    private static final String CODE = "123456";

    @Mock
    private RedisService redisService;

    @Mock
    private SmsSender smsSender;

    @InjectMocks
    private SmsService smsService;

    @Nested
    @DisplayName("sendCode")
    class SendCode {

        @Test
        @DisplayName("쓰로틀 키가 없으면 throttle/code를 Redis에 저장하고 SMS를 발송한다")
        void success_savesKeysAndSendsSms() {
            // given
            given(redisService.hasKey(RedisKeys.smsThrottle(PHONE))).willReturn(false);

            // when
            smsService.sendCode(PHONE);

            // then
            then(redisService).should().set(eq(RedisKeys.smsThrottle(PHONE)), eq(""), eq(Duration.ofMinutes(1)));
            then(redisService).should().set(eq(RedisKeys.smsCode(PHONE)), anyString(), eq(Duration.ofMinutes(3)));
            then(smsSender).should().send(eq(PHONE), anyString());
        }

        @Test
        @DisplayName("쓰로틀 키가 존재하면 SMS_SEND_TOO_MANY_REQUESTS 예외가 발생한다")
        void fail_throttleExists_throwsTooManyRequests() {
            // given
            given(redisService.hasKey(RedisKeys.smsThrottle(PHONE))).willReturn(true);

            // when & then
            assertThatThrownBy(() -> smsService.sendCode(PHONE))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.SMS_SEND_TOO_MANY_REQUESTS);

            then(smsSender).should(never()).send(any(), any());
        }
    }

    @Nested
    @DisplayName("verifyCode")
    class VerifyCode {

        @Test
        @DisplayName("코드가 일치하면 smsCode를 삭제하고 verifiedToken을 반환하며 smsVerified를 저장한다")
        void success_returnsVerifiedTokenAndSavesToRedis() {
            // given
            given(redisService.get(RedisKeys.smsCode(PHONE))).willReturn(CODE);

            // when
            String verifiedToken = smsService.verifyCode(PHONE, CODE);

            // then
            assertThat(verifiedToken).isNotNull();
            then(redisService).should().delete(RedisKeys.smsCode(PHONE));

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
            then(redisService).should().set(keyCaptor.capture(), valueCaptor.capture(), ttlCaptor.capture());

            assertThat(keyCaptor.getValue()).isEqualTo(RedisKeys.smsVerified(verifiedToken));
            assertThat(valueCaptor.getValue()).isEqualTo(PHONE);
            assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofMinutes(10));
        }

        @Test
        @DisplayName("코드가 만료(null)되면 SMS_CODE_EXPIRED 예외가 발생한다")
        void fail_codeExpired_throwsSmsCodeExpired() {
            // given
            given(redisService.get(RedisKeys.smsCode(PHONE))).willReturn(null);

            // when & then
            assertThatThrownBy(() -> smsService.verifyCode(PHONE, CODE))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.SMS_CODE_EXPIRED);
        }

        @Test
        @DisplayName("코드가 불일치하면 SMS_CODE_MISMATCH 예외가 발생한다")
        void fail_codeMismatch_throwsSmsCodeMismatch() {
            // given
            given(redisService.get(RedisKeys.smsCode(PHONE))).willReturn("999999");

            // when & then
            assertThatThrownBy(() -> smsService.verifyCode(PHONE, CODE))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.SMS_CODE_MISMATCH);
        }
    }
}
