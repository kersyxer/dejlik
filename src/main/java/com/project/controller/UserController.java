package com.project.controller;

import com.project.dto.*;
import com.project.entity.User;
import com.project.service.UserException;
import com.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/addUser")
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

    @DeleteMapping("/deleteUser")
    public ResponseEntity<String> deleteUser(@RequestBody DeleteUserRequest request) {
        try {
            userService.deleteUser(request.getId());
            return ResponseEntity.ok("Deleted");
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/updateUser")
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

    @GetMapping("/list")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> dtos = userService.getAllUsers().stream()
                .map(user -> new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole()))
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
