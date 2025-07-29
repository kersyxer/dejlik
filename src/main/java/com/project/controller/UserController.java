package com.project.controller;

import com.project.dto.*;
import com.project.entity.User;
import com.project.service.UserException;
import com.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Boolean> createUser(@RequestBody CreateUserRequest request) {
        try {
            User u = User.builder()
                    .email(request.getEmail())
                    .name(request.getName())
                    .password(request.getPassword())
                    .role(request.getRole())
                    .build();
            userService.createUser(u);
            return ResponseEntity.ok(true);
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteUser(@PathVariable UUID id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(true);
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateUser(@PathVariable UUID id, @RequestBody UpdateUserRequest req) {
        try {
            userService.updateUser(
                    id,
                    req.getName(),
                    req.getPassword(),
                    req.getRole()
            );
            return ResponseEntity.ok(true);
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(false);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> dtos = userService.getAllUsers().stream()
                .map(user -> new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        try {
            User u =  userService.findById(id);
            return ResponseEntity.ok(new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole()));
        }catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
