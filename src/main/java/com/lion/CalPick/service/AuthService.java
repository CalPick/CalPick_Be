package com.lion.CalPick.service;

import com.lion.CalPick.domain.User;
import com.lion.CalPick.domain.UserPrincipal;
import com.lion.CalPick.dto.CheckRequestDto;
import com.lion.CalPick.dto.LoginRequest;
import com.lion.CalPick.dto.LoginResponse;
import com.lion.CalPick.dto.SignUpRequest;
import com.lion.CalPick.repository.UserRepository;
import com.lion.CalPick.util.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public void signup(SignUpRequest signupRequest) {
        if (userRepository.existsByUserId(signupRequest.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        System.out.println(">> 생년월일 birthday-service: " + signupRequest.getBirth());
        User user = new User();
        user.setUserId(signupRequest.getUserId());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setNickname(signupRequest.getNickname());
        user.setBirth(signupRequest.getBirth());
        System.out.println(">> 생년월일 birthday-service2: " + user.getBirth());

        userRepository.save(user);
    }

    @Transactional
    public void check(CheckRequestDto checkRequest) {
        if (userRepository.existsByUserId(checkRequest.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUserId(),
                            loginRequest.getPassword()
                    )
            );
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String accessToken = jwtTokenProvider.generateToken(userPrincipal.getUsername(), userPrincipal.getNickname());
            return new LoginResponse(accessToken, userPrincipal.getNickname(), userPrincipal.getBirth());
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 잘못되었습니다.");
        } catch (Exception e) {
            throw new RuntimeException("로그인 처리 중 서버 오류가 발생했습니다.", e);
        }
    }
}
