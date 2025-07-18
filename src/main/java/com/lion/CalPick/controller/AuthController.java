package com.lion.CalPick.controller;

import com.lion.CalPick.dto.CheckRequestDto;
import com.lion.CalPick.dto.LoginRequest;
import com.lion.CalPick.dto.LoginResponse;
import com.lion.CalPick.dto.SignUpRequest;
import com.lion.CalPick.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest signupRequest) {
        try {
            System.out.println(">> 생년월일 birthday: " + signupRequest.getBirth());
            authService.signup(signupRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("회원가입 성공"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("서버 오류가 발생했습니다."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            return ResponseEntity.ok(loginResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("서버 오류가 발생했습니다."));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> check(@RequestBody CheckRequestDto checkRequest) {
        try {
            authService.check(checkRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("사용 가능한 아이디입니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("서버 오류가 발생했습니다."));
        }
    }

    // Helper classes for consistent JSON responses
    private static class MessageResponse {
        public String message;

        public MessageResponse(String message) {
            this.message = message;
        }
    }

    private static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
