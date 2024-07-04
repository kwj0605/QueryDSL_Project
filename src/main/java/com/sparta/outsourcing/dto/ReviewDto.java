package com.sparta.outsourcing.dto;

import com.sparta.outsourcing.entity.Review;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewDto {

    private Long orderId;
    private String content;
    private Long likes;

    public ReviewDto(Long orderId, String content, Long likes) {
        this.orderId = orderId;
        this.content = content;
        this.likes = likes;
    }

    public static ReviewDto toDto(Review review) {
        return new ReviewDto(
                review.getOrder().getOrderId(),
                review.getContent(),
                review.getLikes()
        );
    }
}
