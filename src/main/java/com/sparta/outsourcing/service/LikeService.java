package com.sparta.outsourcing.service;

import com.sparta.outsourcing.dto.LikeResponseDto;
import com.sparta.outsourcing.dto.RestaurantDto;
import com.sparta.outsourcing.dto.ReviewDto;
import com.sparta.outsourcing.entity.*;
import com.sparta.outsourcing.exception.AlreadySignupException;
import com.sparta.outsourcing.exception.LikeSelfException;
import com.sparta.outsourcing.repository.RestaurantLikeRepository;
import com.sparta.outsourcing.repository.RestaurantRepository;
import com.sparta.outsourcing.repository.ReviewLikeRepository;
import com.sparta.outsourcing.repository.ReviewRepository;
import com.sparta.outsourcing.security.UserDetailsImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final RestaurantLikeRepository restaurantLikeRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final MessageSource messageSource;

    /**
     *
     * @param contentId 특정 식당의 id
     * @param user 인증된 현재 유저
     * @return LikeResponseDto
     */
    public LikeResponseDto updateRestaurantLike(Long contentId, User user) {

        // id에 해당하는 레스토랑 정보 가져오기
        Restaurant restaurant = restaurantRepository.findById(contentId).orElseThrow(() -> new RuntimeException("Restaurant not found"));

        // 본인이 올린 레스토랑인 지 확인
        if (user.getUsername().equals(restaurant.getUser().getUsername())) {
            throw new LikeSelfException(messageSource.getMessage(
                    "like.self", null, "본인이 작성한 컨텐츠에는 좋아요를 등록할 수 없습니다.", Locale.getDefault()
            ));
        }


        RestaurantLike restaurantLike = restaurantLikeRepository.findByUserAndRestaurant(user, restaurant)
                .orElseGet(() -> new RestaurantLike(user, restaurant));
        restaurantLike.update();
        restaurantLikeRepository.save(restaurantLike);

        return calculateRestaurantlike(restaurantLike, restaurant);
    }

    /**
     *
     * @param contentId 특정 리뷰의 id
     * @param user 인증된 현재 유저
     * @return LikeResponseDto
     */
    public LikeResponseDto updateReviewLike(Long contentId, User user) {
        Review review = reviewRepository.findById(contentId).orElseThrow(() -> new RuntimeException("Review not found"));

        // 현재의 사용자와 게시물 등록 사용자 비교
        if (user.getUsername().equals(review.getUser().getUsername())) {
            throw new LikeSelfException(messageSource.getMessage(
                    "like.self", null, "본인이 작성한 컨텐츠에는 좋아요를 등록할 수 없습니다.", Locale.getDefault()
            ));
        }

        ReviewLike reviewLike = reviewLikeRepository.findByUserAndReview(user, review)
                .orElseGet(() -> new ReviewLike(user, review));
        reviewLike.update();
        reviewLikeRepository.save(reviewLike);

        return calculateReviewlike(reviewLike, review);
    }

    // 유저가 좋아요한 게시글 불러오기
    public ResponseEntity<List<RestaurantDto>> getLikedRestaurants(int page, int size, UserDetailsImpl userDetails) {
        Pageable pageable = PageRequest.of(page, size);
        // 로직추가(좋아요가 true인 restaurant)
        Page<Restaurant> likedRestaurantPage = restaurantLikeRepository.findByRestaurantsByUserAndLikedIsTrueOrderByCreatedAtDesc(userDetails.getUser());
        List<RestaurantDto> likedRestaurantDtoList = likedRestaurantPage.getContent().stream()
                .map(this::convertToRestaurantDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(likedRestaurantDtoList);
    }

    // 유저가 좋아요한 리뷰 불러오기
    public ResponseEntity<List<ReviewDto>> getLikedReviews(int page, int size, UserDetailsImpl userDetails) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Review> likedReviewPage = reviewLikeRepository.findByReviewsByUserAndLikedIsTrueOrderByCreatedAtDesc(userDetails.getUser());
        List<ReviewDto> likedReviewDtoList = likedReviewPage.getContent().stream()
                .map(this::convertToReviewDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(likedReviewDtoList);
    }

    private LikeResponseDto calculateRestaurantlike(RestaurantLike restaurantLike, Restaurant restaurant) {
        Long cnt =  restaurant.updateLike(restaurantLike.isLiked());
        return new LikeResponseDto(restaurantLike.isLiked(), cnt);
    }

    public LikeResponseDto calculateReviewlike(ReviewLike reviewLike, Review review) {
        Long cnt =  review.updateLike(reviewLike.isLiked());
        return new LikeResponseDto(reviewLike.isLiked(), cnt);
    }

    private RestaurantDto convertToRestaurantDto(Restaurant restaurant) {
        return new RestaurantDto(restaurant.getRestaurantName(), restaurant.getRestaurantInfo(),
                restaurant.getPhoneNumber(), restaurant.getLikes());
    }

    private ReviewDto convertToReviewDto(Review review) {
        return new ReviewDto(review.getId(), review.getContent(), review.getLikes());
    }


}
