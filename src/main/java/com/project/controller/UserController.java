package com.project.controller;

import com.project.dto.*;
import com.project.entity.User;
import com.project.security.JwtUtil;
import com.project.service.UserException;
import com.project.service.UserService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/users/addUser")
    public ResponseEntity<Boolean> addUser(@RequestBody AddUserRequest request) {
        try {
            User u = User.builder()
                    .email(request.getEmail())
                    .name(request.getName())
                    .password(request.getPassword())
                    .role(request.getRole())
                    .build();
            userService.addUser(u);
            return ResponseEntity.ok(true);
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            boolean ok = userService.loginUser(req.getEmail(), req.getPassword());
            if (!ok) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Incorrect password");
            }
            User user = userService.findByEmail(req.getEmail());
            String accessToken;
            String refreshToken;
            try {
                accessToken = jwtUtil.generateAccessToken(user.getId(), user.getName(), user.getRole());
                refreshToken = jwtUtil.generateRefreshToken(user.getId());
            } catch(JwtException | IllegalArgumentException e) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Token generation failed: " + e.getMessage());
            }
            UserDto dto = new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole());
            return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken, dto));
        } catch (UserException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/users/deleteUser")
    public ResponseEntity<String> deleteUser(@RequestBody DeleteUserRequest request) {
        try {
            userService.deleteUser(request.getId());
            return ResponseEntity.ok("Deleted");
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/users/updateUser")
    public ResponseEntity<String> updateUser(@RequestBody UpdateUserRequest req) {
        try {
            userService.updateUser(
                    req.getId(),
                    req.getName(),
                    req.getPassword(),
                    req.getRole()
            );
            return ResponseEntity.ok("Updated");
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("users/list")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> dtos = userService.getAllUsers().stream()
                .map(user -> new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole()))
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
