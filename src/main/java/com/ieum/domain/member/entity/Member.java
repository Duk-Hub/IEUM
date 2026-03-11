package com.ieum.domain.member.entity;

import com.ieum.domain.member.entity.enums.AuthType;
import com.ieum.domain.member.entity.enums.MemberGrade;
import com.ieum.domain.member.entity.enums.MemberRole;
import com.ieum.domain.member.entity.enums.OAuthProvider;
import com.ieum.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;

@Entity
@Table(
        name = "member",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_member_username", columnNames = "username"),
                @UniqueConstraint(name = "uq_member_phone", columnNames = "phone"),
                @UniqueConstraint(name = "uq_member_nickname", columnNames = "nickname"),
                @UniqueConstraint(name = "uq_member_provider", columnNames = {"provider", "provider_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SoftDelete(columnName = "is_deleted")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthType authType;

    @Column(length = 16)
    private String username;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OAuthProvider provider;

    @Column(length = 100)
    private String providerId;

    @Column(nullable = false, length = 16)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberGrade grade;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;


}