package com.lion.CalPick.repository;

import com.lion.CalPick.domain.Friend;
import com.lion.CalPick.domain.FriendStatus;
import com.lion.CalPick.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    // 친구 요청이 이미 존재하는지 확인 (requester -> addressee)
    Optional<Friend> findByRequesterAndAddressee(User requester, User addressee);

    // 친구 요청을 받았는지 확인 (addressee -> requester)
    Optional<Friend> findByRequesterAndAddresseeAndStatus(User requester, User addressee, FriendStatus status);

    // 특정 사용자의 친구 목록 조회 (ACCEPTED 상태)
    @Query("SELECT f FROM Friend f WHERE (f.requester = :user OR f.addressee = :user) AND f.status = 'ACCEPTED'")
    List<Friend> findAcceptedFriendsByUser(@Param("user") User user);

    // 특정 사용자가 다른 사용자와 친구 관계인지 확인
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN TRUE ELSE FALSE END FROM Friend f WHERE ((f.requester = :user1 AND f.addressee = :user2) OR (f.requester = :user2 AND f.addressee = :user1)) AND f.status = 'ACCEPTED'")
    boolean areFriends(@Param("user1") User user1, @Param("user2") User user2);
}
