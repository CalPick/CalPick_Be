package com.lion.CalPick.controller;

import com.lion.CalPick.domain.User;
import com.lion.CalPick.dto.CreateGroupRequest;
import com.lion.CalPick.dto.CreateGroupResponse;
import com.lion.CalPick.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<CreateGroupResponse> createGroup(
            @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal User user
    ){
        CreateGroupResponse response = groupService.createGroup(request, user.getUserId());
        return ResponseEntity.ok(response);

    }
}
