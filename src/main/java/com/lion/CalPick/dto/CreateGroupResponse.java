package com.lion.CalPick.dto;

import java.time.LocalDateTime;

public record CreateGroupResponse(
        Long groupId,
        String groupName,
        LocalDateTime createdAt
) {}
