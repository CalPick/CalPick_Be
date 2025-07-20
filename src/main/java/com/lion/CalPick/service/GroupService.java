package com.lion.CalPick.service;

import com.lion.CalPick.domain.*;
import com.lion.CalPick.dto.CreateGroupRequest;
import com.lion.CalPick.dto.CreateGroupResponse;
import com.lion.CalPick.dto.GroupResponseDto;
import com.lion.CalPick.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserService userService;
    private final GroupMemberRepository groupMemberRepository;

    private static final int MIN_GROUP_MEMBERS = 3;
    private static final int MAX_GROUP_MEMBERS = 12;

    @Transactional
    public CreateGroupResponse createGroup(CreateGroupRequest request, String requesterId){
        User requester = userRepository.findByUserId(requesterId)
                .orElseThrow(()-> new RuntimeException("사용자 없음"));

        List<String> allMemberIds = new ArrayList<>(request.memberIds());
        allMemberIds.add(requesterId);

        if (allMemberIds.size() < MIN_GROUP_MEMBERS || allMemberIds.size() > MAX_GROUP_MEMBERS){
            throw new IllegalArgumentException("그룹 멤버는 최소 " + MIN_GROUP_MEMBERS + "명, 최대 " + MAX_GROUP_MEMBERS + "명이어야 합니다.");
        }

        for (String memberId : request.memberIds()){
            if(!userRepository.existsByUserId(memberId)){
                throw new IllegalArgumentException("존재하지 않는 사용자입니다: ID" + memberId);
            }
        }

        Set<User> invitedUsers = request.memberIds().stream()
                .map(memberId -> userRepository.findByUserId(memberId)
                        . orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. " + memberId)))
                .collect(Collectors.toSet());

        for (User invitedUser : invitedUsers){
            boolean isFriend = friendRepository.areFriends(requester, invitedUser);
            if (!isFriend) {
                throw new IllegalArgumentException("그룹에 초대할 수 없습니다. " + invitedUser.getUserId() + "님은 친구가 아닙니다.");
            }
        }

        Group group = new Group();
        group.setGroupName(request.groupName());
        group.setCreateAt(LocalDateTime.now());

        for (String userId : allMemberIds){
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("사용자 없음"));

            GroupMember gm = new GroupMember();
            gm.setGroup(group);
            gm.setUser(user);
            gm.setStatus(FriendStatus.ACCEPTED);
            group.getMembers().add(gm);
        }

        group = groupRepository.save(group);

        return new CreateGroupResponse(group.getId(), group.getGroupName(), group.getCreateAt());
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDto> getGroupList(String userId, UserPrincipal currentUser) {
        User owner = userService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!userId.equals(owner.getUserId())) {
            throw new IllegalArgumentException("You are not authorized to access this resource.");
        }

        // 1. 해당 유저가 속한 모든 GroupMember 엔티티 조회
        List<GroupMember> groupMembers = groupMemberRepository.findByUser_UserId(userId);

        // 2. 그룹 ID 기준으로 중복 제거된 Group 리스트 추출
        return groupMembers.stream()
                .map(GroupMember::getGroup)
                .distinct()
                .map(group -> {
                    int memberCount = groupMemberRepository.countByGroup_Id(group.getId()); // ✅ 멤버 수 조회
                    return new GroupResponseDto(
                            group.getId(),
                            group.getGroupName(),
                            memberCount,
                            group.getCreateAt()
                    );
                })
                .collect(Collectors.toList());
    }


}
