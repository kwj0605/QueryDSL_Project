package com.sparta.outsourcing.controller;

import com.sparta.outsourcing.dto.LikeResponseDto;
import com.sparta.outsourcing.dto.RestaurantDto;
import com.sparta.outsourcing.dto.ReviewDto;
import com.sparta.outsourcing.enums.ContentTypeEnum;
import com.sparta.outsourcing.exception.LikeSelfException;
import com.sparta.outsourcing.security.UserDetailsImpl;
import com.sparta.outsourcing.service.LikeService;
import com.sparta.outsourcing.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final RestaurantService restaurantService;

    /**
    *  게시물또는 리뷰에 대한 좋아요 추가 및 취소 컨트롤러
     * @param contentType : {RESTAURANT | REVIEW}
     * @param contentId : RESTAURANT 혹은 REVIEW의 Id
     *
     *                  @return 200 ok
    **/
    @PutMapping("/{contentType}/{contentId}")
    public ResponseEntity<String> updateRestaurantLike(@PathVariable("contentType") ContentTypeEnum contentType, @PathVariable("contentId") Long contentId, @AuthenticationPrincipal UserDetailsImpl userDetails) throws LikeSelfException {

        LikeResponseDto likeResponseDto;

        if (contentType.equals(ContentTypeEnum.RESTAURANT)) {
            likeResponseDto = likeService.updateRestaurantLike(contentId, userDetails.getUser());
        } else {
            likeResponseDto = likeService.updateReviewLike(contentId, userDetails.getUser());
        }

        if (likeResponseDto.isLiked()) {
            return ResponseEntity.ok("좋아요를 눌렀습니다! 다른 콘텐츠도 확인해보세요 : " + likeResponseDto.getCnt());
        } else {
            return ResponseEntity.ok("좋아요를 취소했습니다. 다시 좋아요를 누를 수 있습니다 : " + likeResponseDto.getCnt());
        }
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantDto>> getLikedRestaurants(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return likeService.getLikedRestaurants(page, size, userDetails);
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewDto>> getLikedReviews(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return likeService.getLikedReviews(page, size, userDetails);
    }
}
