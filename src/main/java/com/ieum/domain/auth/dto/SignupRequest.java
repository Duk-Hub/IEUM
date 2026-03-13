package com.ieum.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank
        String verifiedToken,

        @NotBlank
        @Size(min = 2, max = 16, message = "닉네임은 2자 이상 16자 이하입니다.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣_]+$", message = "_를 제외한 특수문자는 사용할 수 없습니다.")
        String nickname,

        @NotBlank
        @Size(min = 4, max = 16, message = "아이디는 4자 이상 16자 이하입니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문, 숫자만 사용 가능합니다.")
        String username,

        @NotBlank
        @Size(min = 8, max = 255, message = "비밀번호는 8자 이상입니다.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).+$",
                message = "비밀번호는 영문 대소문자, 특수문자를 각 1개 이상 포함해야 합니다."
        )
        String password
) {
}
