package com.lion.CalPick.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthBypassController {

    @GetMapping("/")
    public String rootHealth() {
        return "OK";
    }
}