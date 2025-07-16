package com.lion.CalPick.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import com.lion.CalPick.domain.FriendStatus;
import com.lion.CalPick.domain.Group;
import com.lion.CalPick.domain.User;
import com.lion.CalPick.dto.CreateGroupRequest;
import com.lion.CalPick.dto.CreateGroupResponse;
import com.lion.CalPick.repository.FriendRepository;
import com.lion.CalPick.repository.GroupRepository;
import com.lion.CalPick.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    @InjectMocks
    private GroupService groupService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendRepository friendRepository; // Added FriendRepository mock

    private User requester;
    private User member1;
    private User member2;

    @BeforeEach
    void setUp() {
        requester = new User(1L, "user1", "pw", "아연", LocalDate.of(2000, 1, 1));
        member1 = new User(2L, "user2", "pw", "영희", LocalDate.of(2000, 2, 2));
        member2 = new User(3L, "user3", "pw", "철수", LocalDate.of(2000, 3, 3));
    }

    @Test
    void 그룹_정상_생성_테스트(){
        // given
        CreateGroupRequest request = new CreateGroupRequest("우아한 모각코", List.of("user2", "user3"));

        given(userRepository.findByUserId("user1")).willReturn(Optional.of(requester));
        given(userRepository.findByUserId("user2")).willReturn(Optional.of(member1));
        given(userRepository.findByUserId("user3")).willReturn(Optional.of(member2));
        given(userRepository.existsByUserId("user2")).willReturn(true); // Added existsByUserId for member validation
        given(userRepository.existsByUserId("user3")).willReturn(true); // Added existsByUserId for member validation

        given(friendRepository.areFriends(requester, member1)).willReturn(true);
        given(friendRepository.areFriends(requester, member2)).willReturn(true);

        ArgumentCaptor<Group> groupArgumentCaptor = ArgumentCaptor.forClass(Group.class);
        given(groupRepository.save(any(Group.class))).willAnswer(invocation -> {
            Group group = invocation.getArgument(0);
            group.setId(1L);
            return group;
        });

        // when
        CreateGroupResponse response = groupService.createGroup(request, "user1");

        // then
        assertThat(response.groupId()).isEqualTo(1L);
        assertThat(response.groupName()).isEqualTo("우아한 모각코");
        assertThat(response.createdAt()).isNotNull();
        verify(groupRepository).save(groupArgumentCaptor.capture());
        assertThat(groupArgumentCaptor.getValue().getMembers()).hasSize(3); // 요청자 + 2명
        assertThat(groupArgumentCaptor.getValue().getMembers().stream()
                .filter(gm -> gm.getUser().equals(requester))
                .findFirst().orElseThrow().getStatus()).isEqualTo(FriendStatus.ACCEPTED);
        assertThat(groupArgumentCaptor.getValue().getMembers().stream()
                .filter(gm -> gm.getUser().equals(member1))
                .findFirst().orElseThrow().getStatus()).isEqualTo(FriendStatus.ACCEPTED);
        assertThat(groupArgumentCaptor.getValue().getMembers().stream()
                .filter(gm -> gm.getUser().equals(member2))
                .findFirst().orElseThrow().getStatus()).isEqualTo(FriendStatus.ACCEPTED);
    }

    @Test
    void 존재하지_않는_사용자ID가_있으면_예외_발생(){
        // given
        CreateGroupRequest request = new CreateGroupRequest("테스트 그룹", List.of("user2", "user99"));

        given(userRepository.findByUserId("user1")).willReturn(Optional.of(requester));
        given(userRepository.existsByUserId("user2")).willReturn(true);
        given(userRepository.existsByUserId("user99")).willReturn(false); // user99 does not exist

        // when & then
        assertThatThrownBy(()->groupService.createGroup(request, "user1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다: IDuser99"); // Specific error message
    }

    @Test
    void 그룹_멤버_최소_인원_미달_시_예외_발생() {
        // given
        CreateGroupRequest request = new CreateGroupRequest("소규모 그룹", List.of()); // Only requester, 1 member

        given(userRepository.findByUserId("user1")).willReturn(Optional.of(requester));

        // when & then
        assertThatThrownBy(() -> groupService.createGroup(request, "user1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("그룹 멤버는 최소 3명, 최대 12명이어야 합니다.");
    }

    @Test
    void 그룹_멤버_최대_인원_초과_시_예외_발생() {
        // given
        List<String> memberIds = new ArrayList<>();
        for (int i = 2; i <= 13; i++) {
            memberIds.add("user" + i);
            lenient().when(userRepository.existsByUserId("user" + i)).thenReturn(true);
        }
        CreateGroupRequest request = new CreateGroupRequest("대규모 그룹", memberIds);

        given(userRepository.findByUserId("user1")).willReturn(Optional.of(requester));

        // when & then
        assertThatThrownBy(() -> groupService.createGroup(request, "user1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("그룹 멤버는 최소 3명, 최대 12명이어야 합니다.");
    }

    @Test
    void 친구가_아닌_사용자를_초대할_때_예외_발생() {
        // given
        CreateGroupRequest request = new CreateGroupRequest("친구 아닌 그룹", List.of("user2", "user3"));

        given(userRepository.findByUserId("user1")).willReturn(Optional.of(requester));
        lenient().when(userRepository.existsByUserId("user2")).thenReturn(true);
        lenient().when(userRepository.existsByUserId("user3")).thenReturn(true);

        given(userRepository.findByUserId("user2")).willReturn(Optional.of(member1));
        given(userRepository.findByUserId("user3")).willReturn(Optional.of(member2));


        given(friendRepository.areFriends(requester, member1)).willReturn(false);
        given(friendRepository.areFriends(requester, member2)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> groupService.createGroup(request, "user1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("그룹에 초대할 수 없습니다. user2님은 친구가 아닙니다.");
    }
}