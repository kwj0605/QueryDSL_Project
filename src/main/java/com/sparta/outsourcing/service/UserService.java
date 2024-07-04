package com.sparta.outsourcing.service;


import com.sparta.outsourcing.dto.ProfileDto;
import com.sparta.outsourcing.dto.ProfileResponseDto;
import com.sparta.outsourcing.dto.UserDto;
import com.sparta.outsourcing.entity.Restaurant;
import com.sparta.outsourcing.entity.Review;
import com.sparta.outsourcing.entity.User;
import com.sparta.outsourcing.enums.UserRoleEnum;
import com.sparta.outsourcing.enums.UserStatusEnum;
import com.sparta.outsourcing.exception.AlreadySignupException;
import com.sparta.outsourcing.exception.UserNotFoundException;
import com.sparta.outsourcing.repository.RestaurantRepository;
import com.sparta.outsourcing.repository.ReviewRepository;
import com.sparta.outsourcing.repository.UserRepository;
import com.sparta.outsourcing.security.UserDetailsImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;


    public ResponseEntity<String> signUp(UserDto userDto, Long roleId) throws AlreadySignupException {

        Optional<User> checkUsername = userRepository.findByUsername(userDto.getUsername());
        if (checkUsername.isPresent()) {
            throw new AlreadySignupException(messageSource.getMessage(
                    "already.exist", null, "중복된 사용자가 존재합니다.", Locale.getDefault()
            ));
        }
        User user = new User(
                userDto.getUsername(), bCryptPasswordEncoder.encode(userDto.getPassword()),
                userDto.getNickname(), userDto.getUserinfo());
        if(roleId == 1L){
            user.setRole(UserRoleEnum.USER);
        }else if(roleId == 2L){
            user.setRole(UserRoleEnum.ADMIN);
        }else {return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 권한입니다.");}

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body("가입 완료");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ProfileResponseDto> getProfile(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Restaurant> restaurant = restaurantRepository.findById(userId);
        Optional<Review> review = reviewRepository.findById(userId);
        if (user.isPresent()) {
            ProfileResponseDto profileResponseDto = new ProfileResponseDto(
                    user.get().getNickname(), user.get().getUserinfo(),
                    restaurant.get().getLikes(), review.get().getLikes());
            return ResponseEntity.status(HttpStatus.OK).body(profileResponseDto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Transactional
    public ResponseEntity<String> updateProfile(Long userId, ProfileDto profileDto) {
        User user = getUser();
        ProfileDto newProfileDto;

        if (user.getRole().equals(UserRoleEnum.USER)) {
            if(!user.getId().equals(userId)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("다른 유저를 수정할 수 없습니다.");
            }
            if(validatePassword(user,profileDto.getPassword())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("최근 3번안에 사용한 비밀번호는 사용할 수 없습니다.");
            }
            if(bCryptPasswordEncoder.matches(user.getPassword(),profileDto.getPassword())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("현재와 같은 비밀번호는 변경할수 없습니다.");
            }

            newProfileDto = new ProfileDto(
                    profileDto.getNickname(), profileDto.getUserinfo(), bCryptPasswordEncoder.encode(profileDto.getPassword()));
        } else {
            newProfileDto = new ProfileDto(
                    profileDto.getNickname(), profileDto.getUserinfo(), user.getPassword());
        }

        Optional<User> originUser = userRepository.findById(userId);
        if (originUser.isEmpty()) {
            throw new UserNotFoundException("유저를 찾을 수 없습니다.");
        }
        originUser.get().updateProfile(newProfileDto);
        return ResponseEntity.status(HttpStatus.OK).body("수정완료");
    }


    public ResponseEntity<String> signOut(Long userId, HttpServletResponse response) {

        Optional<User> originUser = userRepository.findById(userId);
        if (originUser.isEmpty() || originUser.get().getStatus().equals(UserStatusEnum.DENIED)) {
            throw new UserNotFoundException("유저를 찾을 수 없습니다.");
        }
        User user = getUser();

        if(!validateUser(userId, user.getId())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("다른 유저를 탈퇴할 수 없습니다.");
        }
        user.setStatus(UserStatusEnum.DENIED);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body("탈퇴 완료");
    }

    // 아이디 일치하는지 검증
    private boolean validateUser(Long userId, Long originId) {
        if (!Objects.equals(userId, originId)) {
            return false;
        }
        return true;
    }
    //이전 변경한 비밀번호 검증
    private boolean validatePassword(User user, String password) {
        Hibernate.initialize(user.getDeniedPassword());
        List<String> userPassword = user.getDeniedPassword();
        for (int i = 0; i < userPassword.size(); i++) {
            if (bCryptPasswordEncoder.matches(userPassword.get(i), password)) {
                return true;
            }
        }
        return false;
    }



    private static User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }

        // Principal이 UserDetailsImpl 타입인지 확인
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            throw new IllegalStateException("사용자 정보를 가져올 수 없습니다.");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        User currentUser = userDetails.getUser();
        return currentUser;
    }
}
