package com.lion.CalPick.repository;

import com.lion.CalPick.domain.GroupMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, String> {
    List<GroupMember> findByUser_UserId(String userId);
    List<GroupMember> findByGroup_Id(Long groupId);
    int countByGroup_Id(Long groupId);
}