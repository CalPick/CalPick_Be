package com.lion.CalPick.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    private String userId;
    private String password;
    private String nickname;
}
