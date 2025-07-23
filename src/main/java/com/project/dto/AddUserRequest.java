package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddUserRequest {
    private String email;
    private String name;
    private String password;
    private String role;
}
