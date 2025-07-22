package com.project.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String email;
    private String name;
    private String password;
    private String role;
}
