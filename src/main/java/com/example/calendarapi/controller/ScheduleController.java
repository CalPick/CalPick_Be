package com.example.calendarapi.controller;

import com.example.calendarapi.domain.UserPrincipal;
import com.example.calendarapi.dto.ScheduleRequestDto;
import com.example.calendarapi.dto.ScheduleResponseDto;
import com.example.calendarapi.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponseDto>> getSchedules(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(name = "userId", required = false) String targetUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        List<ScheduleResponseDto> schedules = scheduleService.getSchedules(currentUser, targetUserId, startDate, endDate);
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
