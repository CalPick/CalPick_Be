package com.lion.CalPick.service;
import com.lion.CalPick.domain.GroupMember;
import com.lion.CalPick.domain.Schedule;
import com.lion.CalPick.domain.User;
import com.lion.CalPick.domain.UserPrincipal;
import com.lion.CalPick.dto.GetTop3Response;
import com.lion.CalPick.dto.ScheduleRequestDto;
import com.lion.CalPick.dto.ScheduleResponseDto;
import com.lion.CalPick.dto.AvailableTimeResponseDto;
import com.lion.CalPick.repository.GroupMemberRepository;
import com.lion.CalPick.repository.ScheduleRepository;
import com.lion.CalPick.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    private final ScheduleRepository scheduleRepository;
    private final UserService userService;
    private final FriendService friendService;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, UserService userService, FriendService friendService, UserRepository userRepository, GroupMemberRepository groupMemberRepository) {
        this.scheduleRepository = scheduleRepository;
        this.userService = userService;
        this.friendService = friendService;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    //일정을 블록화
    public static class TimeBlock {
        private final Instant start;
        private final Instant end;

        public TimeBlock(Instant start, Instant end) {
            this.start = start;
            this.end = end;
        }

        public Instant getStart() {
            return start;
        }

        public Instant getEnd() {
            return end;
        }
    }


    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getSchedules(UserPrincipal currentUserPrincipal, String userId, int year, int month) {
        logger.info("getSchedules called. Current User ID: {}, Requested User ID: {}", currentUserPrincipal.getId(), userId);

        ZoneId kstZone = ZoneId.of("Asia/Seoul");

        // 해당year의 해당month의 시작일(1일0시0분)과 끝일(30일/31일 12시 59분)을 정의.
        // 이유는 조회할 일정이 해당 월에 포함되어있는지를 쿼리문으로 계산하기 위함..
        Instant monthStart = LocalDateTime.of(year, month, 1, 0, 0).atZone(kstZone).toInstant();
        Instant monthEnd = LocalDateTime.of(year, month, 1, 0, 0)
                .withDayOfMonth(LocalDateTime.of(year, month, 1, 0, 0).toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).atZone(kstZone).toInstant();

        User currentUser = userService.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));

        User targetUser;
        logger.info("Target user is current user nickname: {}", currentUser.getNickname());
        logger.info("Target user is current user Id: {}", currentUser.getUserId());
        if (currentUser.getUserId().equals(userId)) {
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

        List<Schedule> schedules = scheduleRepository.findOverlappingSchedules(targetUser, monthStart, monthEnd);
        schedules.forEach(schedule -> {
            logger.info("조회된 일정 - User: {}, Title: {}, Start: {}, End: {}", targetUser.getUserId(), schedule.getTitle(), schedule.getStartTime(), schedule.getEndTime());
        });
        return schedules.stream()
                .map(schedule -> new ScheduleResponseDto(
                        schedule.getId(),
                        schedule.getTitle(),
                        schedule.getDescription(),
                        LocalDateTime.ofInstant(schedule.getStartTime(), kstZone),
                        LocalDateTime.ofInstant(schedule.getEndTime(), kstZone),
                        schedule.isRepeating(),
                        schedule.getUser().getUserId(),
                        schedule.getUser().getNickname()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleResponseDto addSchedule(UserPrincipal currentUserPrincipal, ScheduleRequestDto requestDto) {
        User owner = userService.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ZoneId kstZone = ZoneId.of("Asia/Seoul");

        Schedule schedule = new Schedule();
        schedule.setTitle(requestDto.getTitle());
        schedule.setDescription(requestDto.getDescription());
        schedule.setStartTime(requestDto.getStartTime().atZone(kstZone).toInstant());
        schedule.setEndTime(requestDto.getEndTime().atZone(kstZone).toInstant());
        schedule.setRepeating(requestDto.isRepeating());
        schedule.setUser(owner);

        Schedule savedSchedule = scheduleRepository.save(schedule);
        return new ScheduleResponseDto(
                savedSchedule.getId(),
                savedSchedule.getTitle(),
                savedSchedule.getDescription(),
                LocalDateTime.ofInstant(savedSchedule.getStartTime(), kstZone),
                LocalDateTime.ofInstant(savedSchedule.getEndTime(), kstZone),
                savedSchedule.isRepeating(),
                savedSchedule.getUser().getUserId(),
                savedSchedule.getUser().getNickname()
        );
    }

    @Transactional(readOnly = true)
    public AvailableTimeResponseDto getAvailableTimeSlots(Long groupId, String dateString, UserPrincipal userPrincipal) {
        logger.info("getAvailableTimeSlots called for Group ID: {}, Date: {}", groupId, dateString);

        LocalDate targetDate = LocalDate.parse(dateString);
        ZoneId kstZone = ZoneId.of("Asia/Seoul");

        // 1. 그룹 구성원 ID 리스트 조회
        List<GroupMember> groupMembers = groupMemberRepository.findByGroup_Id(groupId);
        if (groupMembers.isEmpty()) {
            return new AvailableTimeResponseDto(dateString, Collections.emptyList());
        }

        // 2. 구성원들의 해당 날짜 일정 조회
        List<List<ScheduleResponseDto>> allMemberSchedules = groupMembers.stream()
                .map(member -> {
                    String userId = member.getUser().getUserId();
                    try {
                        return getSchedulesForSingleDay(userPrincipal, userId, targetDate);
                    } catch (Exception e) {
                        logger.warn("일정 조회 실패: userId={}, 이유={}", userId, e.getMessage());
                        return Collections.<ScheduleResponseDto>emptyList();
                    }
                })
                .collect(Collectors.toList());

        // 3. 모든 구성원의 차단 시간 블록을 추출
        List<TimeBlock> allBlockedBlocks = allMemberSchedules.stream()
                .flatMap(List::stream)
                .map(dto -> new TimeBlock(
                        dto.getStartTime().atZone(kstZone).toInstant(), // Convert LocalDateTime from DTO to Instant
                        dto.getEndTime().atZone(kstZone).toInstant()
                ))
                .sorted(Comparator.comparing(TimeBlock::getStart))
                .collect(Collectors.toList());

        // 4. 병합된 차단 시간대 리스트 생성
        List<TimeBlock> mergedBlockedTimes = new ArrayList<>();
        if (!allBlockedBlocks.isEmpty()) {
            mergedBlockedTimes.add(allBlockedBlocks.get(0));
            for (int i = 1; i < allBlockedBlocks.size(); i++) {
                TimeBlock currentBlock = allBlockedBlocks.get(i);
                TimeBlock lastMergedBlock = mergedBlockedTimes.get(mergedBlockedTimes.size() - 1);

                if (currentBlock.getStart().isBefore(lastMergedBlock.getEnd())) {
                    // Overlap, merge
                    mergedBlockedTimes.set(mergedBlockedTimes.size() - 1,
                            new TimeBlock(lastMergedBlock.getStart(),
                                    currentBlock.getEnd().isAfter(lastMergedBlock.getEnd()) ? currentBlock.getEnd() : lastMergedBlock.getEnd()));
                } else {
                    // No overlap, add new block
                    mergedBlockedTimes.add(currentBlock);
                }
            }
        }

        // 5. `06:00 ~ 24:00` 에서 차단 구간 제외 → 공통 가능한 시간대
        List<AvailableTimeResponseDto.TimeSlot> availableSlots = new ArrayList<>();
        Instant dayStartBoundary = targetDate.atTime(6, 0).atZone(kstZone).toInstant();
        Instant dayEndBoundary = targetDate.atTime(23, 59, 59).atZone(kstZone).toInstant(); // 24:00 is start of next day, 23:59:59 is end of current day

        Instant currentCheckTime = dayStartBoundary;

        for (TimeBlock blockedBlock : mergedBlockedTimes) {
            // If there's a gap before the current blocked block
            if (currentCheckTime.isBefore(blockedBlock.getStart())) {
                availableSlots.add(new AvailableTimeResponseDto.TimeSlot(
                        LocalDateTime.ofInstant(currentCheckTime, kstZone).toLocalTime().toString(),
                        LocalDateTime.ofInstant(blockedBlock.getStart(), kstZone).toLocalTime().toString()
                ));
            }
            // Move currentCheckTime past the blocked block
            currentCheckTime = blockedBlock.getEnd().isAfter(currentCheckTime) ? blockedBlock.getEnd() : currentCheckTime;
        }

        // If there's a remaining gap after the last blocked block until dayEndBoundary
        if (currentCheckTime.isBefore(dayEndBoundary)) {
            availableSlots.add(new AvailableTimeResponseDto.TimeSlot(
                    LocalDateTime.ofInstant(currentCheckTime, kstZone).toLocalTime().toString(),
                    LocalDateTime.ofInstant(dayEndBoundary, kstZone).toLocalTime().toString()
            ));
        }

        // 6. 시간 순 정렬 후 응답 (already sorted by merging logic)
        return new AvailableTimeResponseDto(dateString, availableSlots);
    }

    private List<ScheduleResponseDto> getSchedulesForSingleDay(UserPrincipal currentUserPrincipal, String userId, LocalDate targetDate) {
        logger.info("getSchedulesForSingleDay called. Current User ID: {}, Requested User ID: {}, Target Date: {}", currentUserPrincipal.getId(), userId, targetDate);

        ZoneId kstZone = ZoneId.of("Asia/Seoul");

        Instant dayStart = targetDate.atStartOfDay(kstZone).toInstant();
        Instant dayEnd = targetDate.atTime(23, 59, 59).atZone(kstZone).toInstant(); // End of day

        User currentUser = userService.findById(currentUserPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));

        User targetUser;
        if (currentUser.getUserId().equals(userId)) {
            targetUser = currentUser;
        } else {
            targetUser = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            if (!friendService.areFriends(currentUser, targetUser)) {
                logger.warn("Attempt to access another user's schedule without friend validation. Current User ID: {}, Requested User ID: {}", currentUser.getUserId(), targetUser.getUserId());
                throw new IllegalArgumentException("해당 사용자의 일정에 접근할 수 없습니다.");
            }
        }

        List<Schedule> schedules = scheduleRepository.findOverlappingSchedules(targetUser, dayStart, dayEnd);
        return schedules.stream()
                .map(schedule -> new ScheduleResponseDto(
                        schedule.getId(),
                        schedule.getTitle(),
                        schedule.getDescription(),
                        LocalDateTime.ofInstant(schedule.getStartTime(), kstZone),
                        LocalDateTime.ofInstant(schedule.getEndTime(), kstZone),
                        schedule.isRepeating(),
                        schedule.getUser().getUserId(),
                        schedule.getUser().getNickname()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public GetTop3Response getTop3Schedules(Long groupId, int year, int month, UserPrincipal userPrincipal) {
        logger.info("현재 top3 조회하는 유저 닉네임: {}", userPrincipal.getNickname());
        // 그룹id로 그룹구성원 user_id를 얻은 후 해당 user_id로 getSchedules조회
        List<GroupMember> groupMembers = groupMemberRepository.findByGroup_Id(groupId);
        logger.info("그룹 ID {}에 대한 그룹 멤버 수: {}", groupId, groupMembers.size());

        // 일정 수집
        List<List<ScheduleResponseDto>> allSchedules = groupMembers.stream()
                .map(member -> {
                    String userId = member.getUser().getUserId();
                    logger.info("처리 중인 그룹원 ID: {}", userId);
                    try {
                        logger.info("그룹원 id: {}", userId);
                        return getSchedules(
                                userPrincipal,
                                userId,
                                year,
                                month
                        );
                    } catch (Exception e) {
                        logger.warn("일정 조회 실패: userId={}, 이유={}", userId, e.getMessage());
                        return List.<ScheduleResponseDto>of();
                    }
                })
                .collect(Collectors.toList());

        //전체 일정 통합 및 정렬
        List<TimeBlock> allBlocks = allSchedules.stream()
                .flatMap(List::stream)
                .map(dto -> new TimeBlock(
                        dto.getStartTime().atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                        dto.getEndTime().atZone(ZoneId.of("Asia/Seoul")).toInstant()
                ))
                .sorted(Comparator.comparing(TimeBlock::getStart))
                .collect(Collectors.toList());

        // 겹치는 시간 병합 → 차단 시간대 생성
        List<TimeBlock> blockedTimes = new ArrayList<>();

        for (TimeBlock block : allBlocks) {
            if (blockedTimes.isEmpty()) {
                blockedTimes.add(block);
            } else {
                TimeBlock last = blockedTimes.get(blockedTimes.size() - 1);
                if (block.getStart().isBefore(last.getEnd())) {
                    // 병합
                    blockedTimes.set(blockedTimes.size() - 1,
                            new TimeBlock(last.getStart(), last.getEnd().isAfter(block.getEnd()) ? last.getEnd() : block.getEnd()));
                } else {
                    blockedTimes.add(block);
                }
            }
        }

        // 날짜별로 차단된 시간대 분류 및 모든 날짜 포함
        Map<LocalDate, List<TimeBlock>> dateToBlockedTimes = new HashMap<>();
        for (TimeBlock block : blockedTimes) {
            LocalDate dateKey = LocalDate.ofInstant(block.getStart(), ZoneId.of("Asia/Seoul"));
            dateToBlockedTimes.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(block);
        }

        // 해당 월의 모든 날짜를 고려 대상에 추가 (일정이 없는 날 포함)
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            dateToBlockedTimes.putIfAbsent(date, new ArrayList<>()); // 일정이 없는 날은 빈 리스트로 추가
        }

        // 가능 시간 계산 및 Top3 추출
        List<GetTop3Response.TopDate> topDateList = dateToBlockedTimes.entrySet().stream()
                .map(entry -> {
                    LocalDate localDate = entry.getKey();
                    ZoneId kstZone = ZoneId.of("Asia/Seoul");

                    Instant startOfDay = localDate.atTime(6, 0).atZone(kstZone).toInstant();
                    Instant endOfDay = localDate.atTime(23, 59).atZone(kstZone).toInstant();

                    List<TimeBlock> blocked = entry.getValue();
                    List<GetTop3Response.TimeSlot> availableSlots = new ArrayList<>();
                    Instant current = startOfDay;

                    for (TimeBlock block : blocked) {
                        if (current.isBefore(block.getStart())) {
                            availableSlots.add(new GetTop3Response.TimeSlot(
                                    LocalDateTime.ofInstant(current, kstZone).toLocalTime().toString(),
                                    LocalDateTime.ofInstant(block.getStart(), kstZone).toLocalTime().toString()));
                        }
                        current = block.getEnd().isAfter(current) ? block.getEnd() : current;
                    }
                    if (current.isBefore(endOfDay)) {
                        availableSlots.add(new GetTop3Response.TimeSlot(
                                LocalDateTime.ofInstant(current, kstZone).toLocalTime().toString(),
                                LocalDateTime.ofInstant(endOfDay, kstZone).toLocalTime().toString()));
                    }

                    return new GetTop3Response.TopDate(localDate.toString(), availableSlots);
                })
                .filter(t -> !t.getAvailableSlots().isEmpty())
                .sorted((a, b) -> {
                    ZoneId kstZone = ZoneId.of("Asia/Seoul");
                    long durationA = a.getAvailableSlots().stream()
                            .mapToLong(slot -> {
                                Instant startTime = LocalDate.parse(a.getDate()).atTime(LocalTime.parse(slot.getStart())).atZone(kstZone).toInstant();
                                Instant endTime = LocalDate.parse(a.getDate()).atTime(LocalTime.parse(slot.getEnd())).atZone(kstZone).toInstant();
                                if (slot.getEnd().equals("23:59")) {
                                    endTime = LocalDate.parse(a.getDate()).atTime(LocalTime.of(23, 59, 59)).atZone(kstZone).toInstant();
                                }
                                return Duration.between(startTime, endTime).toMinutes();
                            })
                            .sum();
                    long durationB = b.getAvailableSlots().stream()
                            .mapToLong(slot -> {
                                Instant startTime = LocalDate.parse(b.getDate()).atTime(LocalTime.parse(slot.getStart())).atZone(kstZone).toInstant();
                                Instant endTime = LocalDate.parse(b.getDate()).atTime(LocalTime.parse(slot.getEnd())).atZone(kstZone).toInstant();
                                if (slot.getEnd().equals("23:59")) {
                                    endTime = LocalDate.parse(b.getDate()).atTime(LocalTime.of(23, 59, 59)).atZone(kstZone).toInstant();
                                }
                                return Duration.between(startTime, endTime).toMinutes();
                            })
                            .sum();
                    return Long.compare(durationB, durationA); // 총 시간 길이가 긴 날짜 우선
                })
                .limit(3)
                .collect(Collectors.toList());

        return new GetTop3Response(groupId, topDateList);
    }

    // For testing purposes or initial data setup
    @Transactional
    public Schedule saveSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }
}
