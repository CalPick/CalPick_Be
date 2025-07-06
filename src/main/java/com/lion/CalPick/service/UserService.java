package com.lion.CalPick.service;

import com.lion.CalPick.domain.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findById(Long id);
}
