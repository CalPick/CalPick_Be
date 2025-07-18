package com.lion.CalPick.controller;

import com.lion.CalPick.domain.UserPrincipal;
import com.lion.CalPick.dto.ScheduleRequestDto;
import com.lion.CalPick.dto.ScheduleResponseDto;
import com.lion.CalPick.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<ScheduleResponseDto>> getSchedules(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(name = "userId", required = false) String targetUserId,
            @RequestParam(name = "year", required = false) int year,
            @RequestParam(name = "month", required = false) int month
    ) {
        List<ScheduleResponseDto> schedules = scheduleService.getSchedules(currentUser, targetUserId, year, month);
        return ResponseEntity.ok(schedules);
    }

    @PostMapping
    public ResponseEntity<ScheduleResponseDto> addSchedule(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody ScheduleRequestDto requestDto
    ) {
        ScheduleResponseDto newSchedule = scheduleService.addSchedule(currentUser, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newSchedule);
    }

}
