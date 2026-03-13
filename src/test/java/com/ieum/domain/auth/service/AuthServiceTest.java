package com.ieum.domain.auth.service;

import com.ieum.domain.auth.dto.SignupRequest;
import com.ieum.domain.member.entity.Member;
import com.ieum.domain.member.repository.MemberRepository;
import com.ieum.global.exception.CustomException;
import com.ieum.global.exception.ErrorCode;
import com.ieum.infra.redis.RedisKeys;
import com.ieum.infra.redis.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    private static final String VERIFIED_TOKEN = "test-verified-token";
    private static final String PHONE = "01012345678";
    private static final String USERNAME = "testuser";
    private static final String NICKNAME = "테스트닉네임";
    private static final String RAW_PASSWORD = "Test1234!";
    private static final String ENCODED_PASSWORD = "encoded-password";

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("signup")
    class Signup {

        @Test
        @DisplayName("정상 입력이면 회원을 저장하고 Redis verifiedToken을 삭제한다")
        void success_savesMemberAndDeletesToken() {
            // given
            SignupRequest request = signupRequest(VERIFIED_TOKEN, NICKNAME, USERNAME, RAW_PASSWORD);
            given(redisService.get(RedisKeys.smsVerified(VERIFIED_TOKEN))).willReturn(PHONE);
            given(memberRepository.existsByPhone(PHONE)).willReturn(false);
            given(memberRepository.existsByUsername(USERNAME)).willReturn(false);
            given(memberRepository.existsByNickname(NICKNAME)).willReturn(false);
            given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_PASSWORD);

            // when
            authService.signup(request);

            // then
            InOrder inOrder = inOrder(memberRepository,redisService);
            inOrder.verify(memberRepository).save(any(Member.class));
            inOrder.verify(redisService).delete(RedisKeys.smsVerified(VERIFIED_TOKEN));
        }

        @Test
        @DisplayName("verifiedToken이 Redis에 없으면 SMS_NOT_VERIFIED 예외가 발생한다")
        void fail_tokenNotFound_throwsSmsNotVerified() {
            // given
            SignupRequest request = signupRequest(VERIFIED_TOKEN, NICKNAME, USERNAME, RAW_PASSWORD);
            given(redisService.get(RedisKeys.smsVerified(VERIFIED_TOKEN))).willReturn(null);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.SMS_NOT_VERIFIED);

            then(memberRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 존재하는 전화번호면 PHONE_ALREADY_EXISTS 예외가 발생한다")
        void fail_phoneDuplicated_throwsPhoneAlreadyExists() {
            // given
            SignupRequest request = signupRequest(VERIFIED_TOKEN, NICKNAME, USERNAME, RAW_PASSWORD);
            given(redisService.get(RedisKeys.smsVerified(VERIFIED_TOKEN))).willReturn(PHONE);
            given(memberRepository.existsByPhone(PHONE)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PHONE_ALREADY_EXISTS);

            then(memberRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 존재하는 username이면 USERNAME_ALREADY_EXISTS 예외가 발생한다")
        void fail_usernameDuplicated_throwsUsernameAlreadyExists() {
            // given
            SignupRequest request = signupRequest(VERIFIED_TOKEN, NICKNAME, USERNAME, RAW_PASSWORD);
            given(redisService.get(RedisKeys.smsVerified(VERIFIED_TOKEN))).willReturn(PHONE);
            given(memberRepository.existsByPhone(PHONE)).willReturn(false);
            given(memberRepository.existsByUsername(USERNAME)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USERNAME_ALREADY_EXISTS);

            then(memberRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 존재하는 nickname이면 NICKNAME_ALREADY_EXISTS 예외가 발생한다")
        void fail_nicknameDuplicated_throwsNicknameAlreadyExists() {
            // given
            SignupRequest request = signupRequest(VERIFIED_TOKEN, NICKNAME, USERNAME, RAW_PASSWORD);
            given(redisService.get(RedisKeys.smsVerified(VERIFIED_TOKEN))).willReturn(PHONE);
            given(memberRepository.existsByPhone(PHONE)).willReturn(false);
            given(memberRepository.existsByNickname(NICKNAME)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.NICKNAME_ALREADY_EXISTS);

            then(memberRepository).should(never()).save(any());
        }
    }

    private SignupRequest signupRequest(String verifiedToken, String nickname, String username, String password) {
        return new SignupRequest(verifiedToken, nickname, username, password);
    }
}
