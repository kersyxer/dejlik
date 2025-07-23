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
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("Deleted");
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Integer id, @RequestBody UpdateUserRequest req) {
        try {
            userService.updateUser(
                    id,
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

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Integer id) {
        try {
            User u =  userService.findById(id);
            UserDto dto = new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole());
            return ResponseEntity.ok(dto);
        }catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
