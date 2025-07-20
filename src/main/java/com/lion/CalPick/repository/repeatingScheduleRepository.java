package com.lion.CalPick.repository;

import com.lion.CalPick.domain.RepeatingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface repeatingScheduleRepository extends JpaRepository<RepeatingSchedule, Long> {
}
