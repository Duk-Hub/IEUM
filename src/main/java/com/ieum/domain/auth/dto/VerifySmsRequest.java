package com.ieum.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifySmsRequest(
        @NotBlank
        String phone,

        @NotBlank
        @Size(min = 6, max = 6, message = "인증번호는 6자리입니다.")
        String code
) {
}
