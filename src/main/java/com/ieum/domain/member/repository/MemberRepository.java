package com.ieum.domain.member.repository;

import com.ieum.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByPhone(String phone);
    boolean existsByNickname(String nickname);
    boolean existsByUsername(String username);
}
