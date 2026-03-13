package com.ieum.domain.auth.service;

import com.ieum.domain.auth.dto.SignupRequest;
import com.ieum.domain.member.entity.Member;
import com.ieum.domain.member.repository.MemberRepository;
import com.ieum.global.exception.CustomException;
import com.ieum.global.exception.ErrorCode;
import com.ieum.infra.redis.RedisKeys;
import com.ieum.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequest request) {
        String phone = redisService.get(RedisKeys.smsVerified(request.verifiedToken()));
        if (phone == null) {
            throw new CustomException(ErrorCode.SMS_NOT_VERIFIED);
        }

        if (memberRepository.existsByPhone(phone)) {
            throw new CustomException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
        if (memberRepository.existsByUsername(request.username())) {
            throw new CustomException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = Member.joinLocal(request.username(), encodedPassword, phone, request.nickname());
        memberRepository.save(member);

        redisService.delete(RedisKeys.smsVerified(request.verifiedToken()));
    }
}
