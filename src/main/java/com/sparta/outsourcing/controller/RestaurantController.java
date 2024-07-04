package com.sparta.outsourcing.controller;

import com.sparta.outsourcing.dto.RestaurantDto;
import com.sparta.outsourcing.security.UserDetailsImpl;
import com.sparta.outsourcing.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
public class RestaurantController {

     private final RestaurantService restaurantService;

    /**
     *
     * @param restaurantDto : {식당 이름, 식당 소개, 식당 번호} 정보
     * @param userDetails
     * @return {200 ok | 404 not found}
     */
    @PostMapping
    public ResponseEntity<String> addRestaurant(@RequestBody RestaurantDto restaurantDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return restaurantService.addRestaurant(restaurantDto, userDetails.getUser());
    }

    /**
     *
     * @param restaurantId  : 삭제하고자 하는 특정 식당의 id
     * @return {200 ok | 404 not found}
     */
    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<String> deleteRestaurant(@PathVariable("restaurantId") Long restaurantId) {
        return restaurantService.deleteRestaurant(restaurantId);
    }

    /**
     *
     * @param restaurantId : 조회하고자 하는 특정 식당의 id
     * @return : 특정 식당의 {식당 이름, 식당 소개, 식당 번호, 좋아요 수} 정보
     */
    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantDto> getRestaurant(@PathVariable("restaurantId") Long restaurantId) {
        return restaurantService.getRestaurant(restaurantId);
    }

    /**
     *
     * @param page : default = 0
     * @param size : default = 5
     * @return 각 식당의 {식당 이름, 식당 소개, 식당 번호, 좋아요 수} 조회
     *          생성일자 기준 내림차순으로 정렬
     */
    @GetMapping
    public ResponseEntity<List<RestaurantDto>> getAllRestaurants(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {
        return restaurantService.getAllRestaurants(page, size);
    }


   /* @GetMapping("/liked")
    public ResponseEntity<List<RestaurantDto>> getLikedRestaurants(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return restaurantService.getLikedRestaurants(page, size, userDetails);
    }*/
}
