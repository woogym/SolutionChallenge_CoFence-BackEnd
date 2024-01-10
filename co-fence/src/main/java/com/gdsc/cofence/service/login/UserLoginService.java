package com.gdsc.cofence.service.login;

import com.gdsc.cofence.dto.tokenDto.RenewAccessTokenDto;
import com.gdsc.cofence.dto.userDto.UserEmailDto;
import com.gdsc.cofence.dto.userDto.userRequest.UserAndTokenResponseDto;
import com.gdsc.cofence.entity.user.RoleType;
import com.gdsc.cofence.entity.user.User;
import com.gdsc.cofence.dto.userDto.UserInfo;
import com.gdsc.cofence.entity.user.UserRefreshToken;
import com.gdsc.cofence.exception.ErrorCode;
import com.gdsc.cofence.exception.model.CustomException;
import com.gdsc.cofence.jwt.TokenProvider;
import com.gdsc.cofence.repository.UserRefreshTokenRepository;
import com.gdsc.cofence.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;


@Service
@AllArgsConstructor
public class UserLoginService {

    private final UserRepository userRepository;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final TokenRenewService tokenRenewService;

    @Transactional
    public UserAndTokenResponseDto SignUp(UserInfo userInfo) {

        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail()); // 이메일 중복검사
        if (existingUser.isPresent()) {
            throw new CustomException(ErrorCode.ALREADY_EXIST_EMAIL_EXCEPTION,
                    ErrorCode.ALREADY_EXIST_EMAIL_EXCEPTION.getMessage());
        }

        User user = userRepository.save(User.builder()
                    .userName(userInfo.getName())
                    .email(userInfo.getEmail())
                    .phoneNumber(userInfo.getPhoneNumber())
                    .profileImageUrl(userInfo.getProfileImageUrl())
                    .nationality(userInfo.getNationality())
                    .roleType(RoleType.getRoleTypeOfString(userInfo.getRoleType()))
                    .build()
        );

        // accessToken 생성
        String accessToken = tokenProvider.createAccessToken(user);

        // refreshToken 생성
        String refreshToken = tokenProvider.createRefreshToken(user);

        // refreshToken DB에 저장
        UserRefreshToken userRefreshToken = new UserRefreshToken();
        userRefreshToken.setRefreshToken(refreshToken);
        userRefreshToken.setUser(user);

        // 기존에 발행된 리프레시토큰을 삭제
        // jpa는 삭제할 대상이 존재하지 않아도 예외를 발생시키지 않아서 별도의 예외를 발생시키지 않는다
        userRefreshTokenRepository.deleteByUser(user);

        // 새로운 리프레시토큰을 저장
        userRefreshTokenRepository.save(userRefreshToken);

        return UserAndTokenResponseDto.builder()
                .name(user.getUserName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfileImageUrl())
                .nationality(user.getNationality())
                .roleType(user.getRoleType().toString())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    // 사용자 email을 받아서 사용자 검색하고 그 사용자에 맞는 갱신된 refreshToken, accessToken 반환
    @Transactional
    public UserAndTokenResponseDto login(UserEmailDto userEmailDto) {
        String email = userEmailDto.getEmail();

        User user = userRepository.findByEmail(email) // email로 사용자 정보 찾아서 user에 저장
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_EMAIL_EXCEPTION,
                        ErrorCode.NOT_FOUND_EMAIL_EXCEPTION.getMessage()));

        String renewRefreshToken = tokenProvider.createRefreshToken(user); // 갱신된 refreshToken 생성

        UserRefreshToken userRefreshToken = userRefreshTokenRepository.findByUser_UserSeq(user.getUserSeq())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ID_EXCEPTION,
                        ErrorCode.NOT_FOUND_ID_EXCEPTION.getMessage()));

        userRefreshToken.setRefreshToken(renewRefreshToken);
        userRefreshToken.setUser(user);
        userRefreshTokenRepository.save(userRefreshToken); // 갱신된 refreshToken DB에 저장

        RenewAccessTokenDto renewAccessTokenDto = tokenRenewService.renewAccessToken(renewRefreshToken); // accessToken 갱신
        String renewAccessToken = renewAccessTokenDto.getRenewAccessToken();

        return UserAndTokenResponseDto.builder()
                .name(user.getUserName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfileImageUrl())
                .nationality(user.getNationality())
                .roleType(user.getRoleType().toString())
                .accessToken(renewAccessToken)
                .refreshToken(renewRefreshToken)
                .build();
    }

    @Transactional
    public boolean duplicateInspectionEmail(String email) { // 이메일 중복검사
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void logout(User user) {

        Optional<UserRefreshToken> userRefreshToken = userRefreshTokenRepository.findByUser_UserSeq(user.getUserSeq());

        userRefreshToken.ifPresent(userRefreshTokenRepository::delete);
    }

    public User test(Principal principal) {
        Long id = Long.parseLong(principal.getName());

        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ID_EXCEPTION,
                        ErrorCode.NOT_FOUND_ID_EXCEPTION.getMessage()));
    }
}