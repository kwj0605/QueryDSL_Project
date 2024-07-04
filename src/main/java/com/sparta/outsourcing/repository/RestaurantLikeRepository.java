package com.sparta.outsourcing.repository;

import com.sparta.outsourcing.entity.Restaurant;
import com.sparta.outsourcing.entity.RestaurantLike;
import com.sparta.outsourcing.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantLikeRepository extends JpaRepository<RestaurantLike, Long> {
    Optional<RestaurantLike> findByUserAndRestaurant(User user, Restaurant restaurant);
    Page<Restaurant> findByRestaurantsByUserAndLikedIsTrueOrderByCreatedAtDesc(User user);
}
