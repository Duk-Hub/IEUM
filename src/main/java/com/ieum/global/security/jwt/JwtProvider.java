package com.ieum.global.security.jwt;

import com.ieum.domain.member.entity.enums.MemberRole;
import com.ieum.global.exception.CustomException;
import com.ieum.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_REFRESH = "REFRESH";

    private final JwtProperties jwtProperties;

    private SecretKey secretKey;
    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.issuer())
                .build();
    }

    public String generateAccessToken(Long memberId, MemberRole role) {
        return buildToken(memberId, role, TYPE_ACCESS, jwtProperties.accessTokenTtl());
    }

    public String generateRefreshToken(Long memberId, MemberRole role) {
        return buildToken(memberId, null, TYPE_REFRESH, jwtProperties.refreshTokenTtl());
    }

    public Long getMemberId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public MemberRole getRole(String token) {
        String role = parseClaims(token).get(CLAIM_ROLE, String.class);
        return MemberRole.valueOf(role);
    }

    public long getRemainTtl(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    public void validateAccessToken(String token) {
        Claims claims = parseClaims(token);
        if (!TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    public void validateRefreshToken(String token) {
        Claims claims = parseClaims(token);
        if (!TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    private String buildToken(Long memberId, MemberRole role, String type, Duration ttl) {
        Date now = new Date();
        JwtBuilder builder = Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl.toMillis()))
                .claim(CLAIM_TYPE, type)
                .signWith(secretKey);

        if (role != null) {
            builder.claim(CLAIM_ROLE, role.name());
        }

        return builder.compact();
    }

    private Claims parseClaims(String token) {
        try {
            return jwtParser
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }
}
