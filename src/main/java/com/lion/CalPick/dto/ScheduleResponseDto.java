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
    private boolean isRepeating;
    private String userId;
    private String nickname;
    private String color;

    public ScheduleResponseDto(Long id, String title, LocalDateTime startTime, LocalDateTime endTime, boolean isRepeating, String userId, String nickname, String color) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isRepeating = isRepeating;
        this.userId = userId;
        this.nickname = nickname;
        this.color = color;
    }
}
