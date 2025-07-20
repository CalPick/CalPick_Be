package com.lion.CalPick.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class GroupResponseDto {
    private Long groupId;
    private String groupName;
    private int memberCount;
    private LocalDateTime createdAt;
}
