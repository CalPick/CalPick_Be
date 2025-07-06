package com.lion.CalPick.service;

import com.lion.CalPick.domain.Friend;
import com.lion.CalPick.domain.FriendStatus;
import com.lion.CalPick.domain.User;
import com.lion.CalPick.domain.UserPrincipal;
import com.lion.CalPick.dto.FriendRequestDto;
import com.lion.CalPick.dto.FriendResponseDto;
import com.lion.CalPick.repository.FriendRepository;
import com.lion.CalPick.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public FriendService(FriendRepository friendRepository, UserRepository userRepository, UserService userService) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional
    public FriendResponseDto sendFriendRequest(UserPrincipal currentUserPrincipal, FriendRequestDto requestDto) {
        User requester = userService.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Requester not found"));

        User addressee = userRepository.findByUserId(requestDto.getTargetUserId())
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));

        if (requester.equals(addressee)) {
            throw new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        // 이미 친구 요청을 보냈거나, 이미 친구인 경우
        if (friendRepository.findByRequesterAndAddressee(requester, addressee).isPresent() ||
            friendRepository.findByRequesterAndAddressee(addressee, requester).isPresent()) {
            throw new IllegalArgumentException("이미 친구 요청을 보냈거나 친구입니다.");
        }

        Friend friendRequest = new Friend();
        friendRequest.setRequester(requester);
        friendRequest.setAddressee(addressee);
        friendRequest.setStatus(FriendStatus.PENDING);

        Friend savedFriendRequest = friendRepository.save(friendRequest);
        return new FriendResponseDto(savedFriendRequest);
    }

    @Transactional
    public FriendResponseDto acceptFriendRequest(UserPrincipal currentUserPrincipal, Long requestId) {
        Friend friendRequest = friendRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        User currentUser = userService.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));

        if (!friendRequest.getAddressee().equals(currentUser)) {
            throw new IllegalArgumentException("해당 친구 요청을 수락할 권한이 없습니다.");
        }

        if (friendRequest.getStatus() != FriendStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 친구 요청입니다.");
        }

        friendRequest.setStatus(FriendStatus.ACCEPTED);
        Friend acceptedFriend = friendRepository.save(friendRequest);
        return new FriendResponseDto(acceptedFriend);
    }

    @Transactional
    public FriendResponseDto rejectFriendRequest(UserPrincipal currentUserPrincipal, Long requestId) {
        Friend friendRequest = friendRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        User currentUser = userService.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));

        if (!friendRequest.getAddressee().equals(currentUser)) {
            throw new IllegalArgumentException("해당 친구 요청을 거절할 권한이 없습니다.");
        }

        if (friendRequest.getStatus() != FriendStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 친구 요청입니다.");
        }

        friendRequest.setStatus(FriendStatus.REJECTED);
        Friend rejectedFriend = friendRepository.save(friendRequest);
        return new FriendResponseDto(rejectedFriend);
    }

    @Transactional(readOnly = true)
    public List<FriendResponseDto> getFriends(UserPrincipal currentUserPrincipal) {
        User currentUser = userService.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));

        List<Friend> friends = friendRepository.findAcceptedFriendsByUser(currentUser);

        return friends.stream()
                .map(friend -> {
                    User otherUser = friend.getRequester().equals(currentUser) ? friend.getAddressee() : friend.getRequester();
                    return new FriendResponseDto(otherUser, FriendStatus.ACCEPTED);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean areFriends(User user1, User user2) {
        return friendRepository.areFriends(user1, user2);
    }
}
