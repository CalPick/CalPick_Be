package com.lion.CalPick.controller;

import com.lion.CalPick.domain.UserPrincipal;
import com.lion.CalPick.dto.FriendRequestDto;
import com.lion.CalPick.dto.FriendResponseDto;
import com.lion.CalPick.service.FriendService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    // 친구 요청 보내기
    @PostMapping("/request")
    public ResponseEntity<Object> sendFriendRequest(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody FriendRequestDto requestDto
    ) {
        try {
            FriendResponseDto response = friendService.sendFriendRequest(currentUser, requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ErrorResponse("친구 요청 중 서버 오류가 발생했습니다."));
        }
    }

    // 친구 요청 수락
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<Object> acceptFriendRequest(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long requestId
    ) {
        try {
            FriendResponseDto response = friendService.acceptFriendRequest(currentUser, requestId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ErrorResponse("친구 요청 수락 중 서버 오류가 발생했습니다."));
        }
    }

    // 친구 요청 거절
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<Object> rejectFriendRequest(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long requestId
    ) {
        try {
            FriendResponseDto response = friendService.rejectFriendRequest(currentUser, requestId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ErrorResponse("친구 요청 거절 중 서버 오류가 발생했습니다."));
        }
    }

    // 친구 목록 조회
    @GetMapping
    public ResponseEntity<Object> getFriends(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        try {
            List<FriendResponseDto> friends = friendService.getFriends(currentUser);
            return ResponseEntity.ok(friends);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ErrorResponse("친구 목록 조회 중 서버 오류가 발생했습니다."));
        }
    }

    // Helper class for consistent JSON error responses
    private static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
