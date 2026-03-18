package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.request.UserRequestDTO;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.security.JwtUtil;
import com.munashechipanga.eharvest.services.UserServiceImpl;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRequestDTO dto) {

        UserResponseDTO saved = userService.createUser(dto);

        return ResponseEntity.ok(saved);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // authenticate username + password
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // get the actual User entity
        User user = userService.findByUsername(request.getUsername());

        // generate JWT
        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new LoginResponse(token, user.getRole(), user.getId()));
    }
}

@Getter
@Setter
class LoginRequest {
    private String username;
    private String password;
}

@Getter
@Setter
class LoginResponse {
    private String token;
    private String role;
    private Long userId;

    public LoginResponse(String token, String role, Long userId) {
        this.token = token;
        this.role = role;
        this.userId = userId;
    }
}
