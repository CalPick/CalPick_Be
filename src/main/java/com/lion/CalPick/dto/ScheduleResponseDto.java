package com.lion.CalPick.dto;

import com.lion.CalPick.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleResponseDto {
    private Long id;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean repeating;
    private String color;
    
    private String repeatingId;

    public ScheduleResponseDto(Long id, String title, LocalDateTime startTime, LocalDateTime endTime, boolean repeating,  String color, String repeatingId) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.repeating = repeating;
        this.color = color;
        this.repeatingId = repeatingId;
    }
}
