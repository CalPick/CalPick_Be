package com.lion.CalPick.repository;

import com.lion.CalPick.domain.Schedule;
import com.lion.CalPick.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUserAndStartTimeBetween(User user, LocalDateTime startTime, LocalDateTime endTime);
}
