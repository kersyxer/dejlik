package com.project.controller;

import com.project.dto.LoginRequest;
import com.project.dto.LoginResponse;
import com.project.dto.UserDto;
import com.project.entity.User;
import com.project.security.JwtUtil;
import com.project.service.UserException;
import com.project.service.UserService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            User user = userService.findByEmail(req.getEmail());
            boolean ok = userService.loginUser(req.getEmail(),  req.getPassword());
            if(!ok){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect password");
            }
            String accessToken;
            String refreshToken;
            try {
                accessToken = jwtUtil.generateAccessToken(user.getId(), user.getName(), user.getRole());
                refreshToken = jwtUtil.generateRefreshToken(user.getId());
            } catch(JwtException | IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token generation failed: " + e.getMessage());
            }
            UserDto dto = new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole());
            return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken, dto));
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
