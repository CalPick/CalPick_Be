package com.lion.CalPick.dto;

import com.lion.CalPick.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

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
    private String color;
  
    public ScheduleResponseDto(Schedule schedule) {
        ZoneId kstZone = ZoneId.of("Asia/Seoul");
        this.id = schedule.getId();
        this.title = schedule.getTitle();
        this.description = schedule.getDescription();
        this.startTime = LocalDateTime.ofInstant(schedule.getStartTime(), kstZone);
        this.endTime = LocalDateTime.ofInstant(schedule.getEndTime(), kstZone);
        this.isRepeating = schedule.isRepeating();
        this.userId = schedule.getUser().getUserId();
        this.nickname = schedule.getUser().getNickname();
        this.color = schedule.getColor();
    }

    public ScheduleResponseDto(Long id, String title, String description,
                               LocalDateTime startTime, LocalDateTime endTime,
                               boolean isRepeating, String userId, String nickname, String color) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isRepeating = isRepeating;
        this.userId = userId;
        this.nickname = nickname;
        this.color = color;
    }
}
