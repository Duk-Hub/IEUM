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
    @Column(nullable = false)
    private Long version;

    private Member(AuthType authType, String phone, String nickname) {
        this.authType = authType;
        this.phone = phone;
        this.nickname = nickname;
        this.role = MemberRole.USER;
        this.score = 0;
        this.grade = MemberGrade.SEED;
    }


    public static Member joinLocal(String username, String password, String phone, String nickname){
        Member member = new Member(AuthType.LOCAL, phone, nickname);
        member.username = username;
        member.password = password;
        return member;
    }

    public static Member joinSocial(OAuthProvider provider, String providerId, String phone, String nickname){
        Member member = new Member(AuthType.SOCIAL, phone, nickname);
        member.provider = provider;
        member.providerId = providerId;
        return member;
    }

    public void prepareWithdraw(){
        String suffix = "DEL_" + this.id;
        if (this.authType == AuthType.LOCAL){
            this.username = suffix;
            this.password = suffix;
        }
        if (this.authType == AuthType.SOCIAL){
            this.providerId = suffix;
        }
        this.nickname = suffix;
        this.phone = suffix;
    }

    public void updateNickname(String nickname){
        this.nickname = nickname;
    }

    public void updatePassword(String password){
        this.password = password;
    }

    public void editScore(int score){
        this.score = score;
        this.grade = ratingGrade();
    }

    private MemberGrade ratingGrade(){
        if (this.score >= 300){
            return MemberGrade.FOREST;
        } else if (this.score >= 150){
            return MemberGrade.TREE;
        } else if (this.score >= 50){
            return MemberGrade.SPROUT;
        } else return MemberGrade.SEED;
    }
}