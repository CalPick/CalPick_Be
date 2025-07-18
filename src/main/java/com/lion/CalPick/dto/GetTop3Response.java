package com.lion.CalPick.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetTop3Response {
    private Long groupId;
    private List<TopDate> topDates;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopDate {
        private String date;  // 예: "2025-07-05"
        private List<TimeSlot> availableSlots;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private String start; // 예: "09:00"
        private String end;   // 예: "11:30"
    }
}
