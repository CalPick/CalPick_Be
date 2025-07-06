package com.example.calendarapi.service;

import com.example.calendarapi.domain.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findById(Long id);
}
