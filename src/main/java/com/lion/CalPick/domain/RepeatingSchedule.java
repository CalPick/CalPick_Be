package com.lion.CalPick.domain;

import jakarta.persistence.*;
        import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "repeating_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepeatingSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
