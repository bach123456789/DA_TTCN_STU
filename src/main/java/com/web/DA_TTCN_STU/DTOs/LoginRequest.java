package com.web.DA_TTCN_STU.DTOs;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
