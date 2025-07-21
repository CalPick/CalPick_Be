package com.lion.CalPick.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleRequestDto {
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean repeating;
    private String color;
    
    private String repeatingId;
}
