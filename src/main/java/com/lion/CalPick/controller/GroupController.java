package com.lion.CalPick.controller;

import com.lion.CalPick.domain.UserPrincipal;
import com.lion.CalPick.dto.CreateGroupRequest;
import com.lion.CalPick.dto.CreateGroupResponse;
import com.lion.CalPick.dto.GetTop3Response;
import com.lion.CalPick.dto.AvailableTimeResponseDto;
import com.lion.CalPick.service.GroupService;
import com.lion.CalPick.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<CreateGroupResponse> createGroup(
            @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        CreateGroupResponse response = groupService.createGroup(request, userPrincipal.getUsername());
        return ResponseEntity.ok(response);

    }

    @GetMapping("/{groupId}/available-dates")
    public ResponseEntity<GetTop3Response> getTop3(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(name = "year", required = false) int year,
            @RequestParam(name = "month", required = false) int month,
            @PathVariable Long groupId
    ){
        GetTop3Response response = scheduleService.getTop3Schedules(groupId, year, month, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}/available-time")
    public ResponseEntity<AvailableTimeResponseDto> getAvailableTime(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long groupId,
            @RequestParam String date
    ) {
        AvailableTimeResponseDto response = scheduleService.getAvailableTimeSlots(groupId, date, currentUser);
        return ResponseEntity.ok(response);
    }
}
