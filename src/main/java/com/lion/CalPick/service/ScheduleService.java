package com.lion.CalPick.service;

import com.lion.CalPick.domain.Schedule;
import com.lion.CalPick.domain.User;
import com.lion.CalPick.domain.UserPrincipal;
import com.lion.CalPick.dto.ScheduleRequestDto;
import com.lion.CalPick.dto.ScheduleResponseDto;
import com.lion.CalPick.repository.ScheduleRepository;
import com.lion.CalPick.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    private final ScheduleRepository scheduleRepository;
    private final UserService userService;
    private final FriendService friendService;
    private final UserRepository userRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, UserService userService, FriendService friendService, UserRepository userRepository) {
        this.scheduleRepository = scheduleRepository;
        this.userService = userService;
        this.friendService = friendService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getSchedules(UserPrincipal currentUserPrincipal, String userId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("getSchedules called. Current User ID: {}, Requested User ID: {}", currentUserPrincipal.getId(), userId);

        User currentUser = userService.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));

        User targetUser;
        if (userId == null || currentUser.getUserId().equals(userId)) {
            // userId 생략 시 또는 본인 userId 입력 시 본인 일정 조회
            targetUser = currentUser;
            logger.info("Target user is current user: {}", targetUser.getUserId());
        } else {
            // 다른 사용자 일정 조회 시도
            targetUser = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // 친구 관계 검증
            if (!friendService.areFriends(currentUser, targetUser)) {
                logger.warn("Attempt to access another user's schedule without friend validation. Current User ID: {}, Requested User ID: {}", currentUser.getUserId(), targetUser.getUserId());
                throw new IllegalArgumentException("해당 사용자의 일정에 접근할 수 없습니다.");
            }
            logger.info("Target user is a friend. User ID: {}", targetUser.getUserId());
        }

        List<Schedule> schedules = scheduleRepository.findByUserAndStartTimeBetween(targetUser, startDate, endDate);
        return schedules.stream()
                .map(ScheduleResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleResponseDto addSchedule(UserPrincipal currentUserPrincipal, ScheduleRequestDto requestDto) {
        User owner = userService.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Schedule schedule = new Schedule();
        schedule.setTitle(requestDto.getTitle());
        schedule.setDescription(requestDto.getDescription());
        schedule.setStartTime(requestDto.getStartTime());
        schedule.setEndTime(requestDto.getEndTime());
        schedule.setRepeating(requestDto.isRepeating());
        schedule.setUser(owner);

        Schedule savedSchedule = scheduleRepository.save(schedule);
        return new ScheduleResponseDto(savedSchedule);
    }

    // For testing purposes or initial data setup
    @Transactional
    public Schedule saveSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }
}
