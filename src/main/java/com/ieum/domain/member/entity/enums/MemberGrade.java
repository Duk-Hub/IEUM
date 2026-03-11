package com.ieum.domain.member.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberGrade {
    SEED("씨앗"),
    SPROUT("새싹"),
    TREE("나무"),
    FOREST("숲");

    private final String description;
}
