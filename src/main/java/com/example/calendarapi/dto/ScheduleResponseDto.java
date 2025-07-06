package com.example.calendarapi.dto;

import com.example.calendarapi.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleResponseDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isRepeating;
    private String userId;
    private String nickname;

    public ScheduleResponseDto(Schedule schedule) {
        this.id = schedule.getId();
        this.title = schedule.getTitle();
        this.description = schedule.getDescription();
        this.startTime = schedule.getStartTime();
        this.endTime = schedule.getEndTime();
        this.isRepeating = schedule.isRepeating();
        this.userId = schedule.getUser().getUserId();
        this.nickname = schedule.getUser().getNickname();
    }
}
