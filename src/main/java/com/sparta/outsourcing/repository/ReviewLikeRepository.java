package com.sparta.outsourcing.repository;

import com.sparta.outsourcing.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    Optional<ReviewLike> findByUserAndReview(User user, Review review);
    Page<Review> findByReviewsByUserAndLikedIsTrueOrderByCreatedAtDesc(User user);
}
