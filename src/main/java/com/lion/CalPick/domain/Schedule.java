package com.lion.CalPick.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;


import java.time.Instant;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor

public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Instant startTime;
    private Instant endTime;
    @Column(nullable = false)
    private boolean repeating = false;
    private String color;
    private String repeatingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
