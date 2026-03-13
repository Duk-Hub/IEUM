package com.ieum.global.security.jwt;

import com.ieum.domain.member.entity.enums.MemberRole;
import com.ieum.global.exception.CustomException;
import com.ieum.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtProvider")
class JwtProviderTest {

    private static final Long MEMBER_ID = 1L;
    private static final MemberRole ROLE = MemberRole.USER;
    private static final String ISSUER = "ieum-test";
    private static final String SECRET = "testSecretKeyForJwtProviderTestBase64Encoded32Characters!";
    private static final Duration ACCESS_TTL = Duration.ofMinutes(30);
    private static final Duration REFRESH_TTL = Duration.ofDays(14);

    private JwtProvider jwtProvider;
    private JwtProvider expiredJwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = createJwtProvider(ACCESS_TTL, REFRESH_TTL);
        expiredJwtProvider = createJwtProvider(Duration.ofMillis(-1), Duration.ofMillis(-1));
    }

    private JwtProvider createJwtProvider(Duration accessTtl, Duration refreshTtl) {
        JwtProperties properties = new JwtProperties(ISSUER, SECRET, accessTtl, refreshTtl);
        JwtProvider provider = new JwtProvider(properties);
        provider.init();
        return provider;
    }

    @Nested
    @DisplayName("AT 생성")
    class GenerateAccessToken {

        @Test
        @DisplayName("생성된 AT에서 memberId를 정상 파싱한다")
        void success_memberIdMatches() {
            // when
            String token = jwtProvider.generateAccessToken(MEMBER_ID, ROLE);

            // then
            assertThat(jwtProvider.getMemberId(token)).isEqualTo(MEMBER_ID);
        }

        @Test
        @DisplayName("생성된 AT에서 role을 정상 파싱한다")
        void success_roleMatches() {
            // when
            String token = jwtProvider.generateAccessToken(MEMBER_ID, ROLE);

            // then
            assertThat(jwtProvider.getRole(token)).isEqualTo(ROLE);
        }
    }

    @Nested
    @DisplayName("RT 생성")
    class GenerateRefreshToken {

        @Test
        @DisplayName("생성된 RT에서 memberId를 정상 파싱한다")
        void success_memberIdMatches() {
            // when
            String token = jwtProvider.generateRefreshToken(MEMBER_ID);

            // then
            assertThat(jwtProvider.getMemberId(token)).isEqualTo(MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("AT 검증")
    class ValidateAccessToken {

        @Test
        @DisplayName("AT이면 예외 없이 통과한다")
        void success_noException() {
            // given
            String token = jwtProvider.generateAccessToken(MEMBER_ID, ROLE);

            // when & then
            jwtProvider.validateAccessToken(token);
        }

        @Test
        @DisplayName("RT를 넘기면 TOKEN_INVALID 예외가 발생한다")
        void fail_refreshToken_throwsTokenInvalid() {
            // given
            String refreshToken = jwtProvider.generateRefreshToken(MEMBER_ID);

            // when & then
            assertThatThrownBy(() -> jwtProvider.validateAccessToken(refreshToken))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TOKEN_INVALID);
        }
    }

    @Nested
    @DisplayName("RT 검증")
    class ValidateRefreshToken {

        @Test
        @DisplayName("RT이면 예외 없이 통과한다")
        void success_noException() {
            // given
            String token = jwtProvider.generateRefreshToken(MEMBER_ID);

            // when & then
            jwtProvider.validateRefreshToken(token);
        }

        @Test
        @DisplayName("AT를 넘기면 TOKEN_INVALID 예외가 발생한다")
        void fail_accessToken_throwsTokenInvalid() {
            // given
            String accessToken = jwtProvider.generateAccessToken(MEMBER_ID, ROLE);

            // when & then
            assertThatThrownBy(() -> jwtProvider.validateRefreshToken(accessToken))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TOKEN_INVALID);
        }
    }

    @Nested
    @DisplayName("getRemainTtl")
    class GetRemainTtl {

        @Test
        @DisplayName("생성 직후 AT의 TTL은 양수다")
        void success_freshToken_positive() {
            // given
            String token = jwtProvider.generateAccessToken(MEMBER_ID, ROLE);

            // when
            long remainTtl = jwtProvider.getRemainTtl(token);

            // then
            assertThat(remainTtl).isPositive();
        }
    }

    @Nested
    @DisplayName("토큰 파싱 실패")
    class ParseFailure {

        @Test
        @DisplayName("만료된 토큰이면 TOKEN_EXPIRED 예외가 발생한다")
        void fail_expiredToken_throwsTokenExpired() {
            // given
            String expiredToken = expiredJwtProvider.generateAccessToken(MEMBER_ID, ROLE);

            // when & then
            assertThatThrownBy(() -> jwtProvider.getMemberId(expiredToken))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TOKEN_EXPIRED);
        }

        @Test
        @DisplayName("변조된 토큰이면 TOKEN_INVALID 예외가 발생한다")
        void fail_tamperedToken_throwsTokenInvalid() {
            // given
            String tamperedToken = "eyJhbGciOiJIUzI1NiJ9.invalid.payload";

            // when & then
            assertThatThrownBy(() -> jwtProvider.getMemberId(tamperedToken))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TOKEN_INVALID);
        }
    }
}
