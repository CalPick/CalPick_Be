package com.lion.CalPick.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AvailableTimeResponseDto {
    private String date;
    private List<TimeSlot> availableSlots;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class TimeSlot {
        private String start;
        private String end;
    }
}