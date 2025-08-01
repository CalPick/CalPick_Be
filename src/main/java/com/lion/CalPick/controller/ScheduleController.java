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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);
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
        logger.info("여기까지 진입성공");
        ScheduleResponseDto newSchedule = scheduleService.addSchedule(currentUser, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newSchedule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponseDto> updateSchedule(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @RequestBody ScheduleRequestDto requestDto
    ) {
        ScheduleResponseDto updatedSchedule = scheduleService.updateSchedule(currentUser, id, requestDto);
        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        scheduleService.deleteSchedule(currentUser, id);
        return ResponseEntity.noContent().build();
    }

}
