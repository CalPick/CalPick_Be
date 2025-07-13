package com.lion.CalPick.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SignUpRequest {
    private String userId;
    private String password;
    private String nickname;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;
}
