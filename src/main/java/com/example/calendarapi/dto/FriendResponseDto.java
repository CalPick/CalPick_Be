package com.example.calendarapi.dto;

import com.example.calendarapi.domain.Friend;
import com.example.calendarapi.domain.FriendStatus;
import com.example.calendarapi.domain.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendResponseDto {
    private Long id;
    private String requesterUserId;
    private String requesterNickname;
    private String addresseeUserId;
    private String addresseeNickname;
    private FriendStatus status;

    public FriendResponseDto(Friend friend) {
        this.id = friend.getId();
        this.requesterUserId = friend.getRequester().getUserId();
        this.requesterNickname = friend.getRequester().getNickname();
        this.addresseeUserId = friend.getAddressee().getUserId();
        this.addresseeNickname = friend.getAddressee().getNickname();
        this.status = friend.getStatus();
    }

    public FriendResponseDto(User user, FriendStatus status) {
        this.addresseeUserId = user.getUserId();
        this.addresseeNickname = user.getNickname();
        this.status = status;
    }
}
