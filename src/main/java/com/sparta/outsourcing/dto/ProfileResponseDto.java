package com.sparta.outsourcing.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileResponseDto {
    private String nickname;
    private String userinfo;
    private Long restaurantLikes;
    private Long reviewLikes;

    public ProfileResponseDto(String nickname, String userinfo, Long restaurantLikes, Long reviewLikes) {
        this.nickname = nickname;
        this.userinfo = userinfo;
        this.restaurantLikes = restaurantLikes;
        this.reviewLikes = reviewLikes;
    }
}
