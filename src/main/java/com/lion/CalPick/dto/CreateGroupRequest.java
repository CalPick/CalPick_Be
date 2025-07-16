package com.lion.CalPick.dto;

import java.util.List;

public record CreateGroupRequest(
        String groupName,
        List<String> memberIds
) {}
