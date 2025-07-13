package com.lion.CalPick.repository;

import com.lion.CalPick.domain.Schedule;
import com.lion.CalPick.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("SELECT s FROM Schedule s WHERE s.user = :user AND s.endTime >= :monthStart AND s.startTime <= :monthEnd")
    List<Schedule> findOverlappingSchedules(@Param("user") User user, @Param("monthStart") LocalDateTime monthStart, @Param("monthEnd") LocalDateTime monthEnd);

}
