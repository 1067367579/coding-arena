package com.example.friend.domain.dto;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String email;
    private String code;
}
