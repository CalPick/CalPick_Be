package com.lion.CalPick.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleRequestDto {
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isRepeating;
    private String color;
}
