package com.munashechipanga.eharvest.controllers;

import com.munashechipanga.eharvest.dtos.request.UserRequestDTO;
import com.munashechipanga.eharvest.dtos.response.UserResponseDTO;
import com.munashechipanga.eharvest.entities.User;
import com.munashechipanga.eharvest.security.JwtUtil;
import com.munashechipanga.eharvest.services.RefreshTokenService;
import com.munashechipanga.eharvest.services.UserServiceImpl;
import com.munashechipanga.eharvest.services.VerificationService;
import com.munashechipanga.eharvest.dtos.request.RefreshTokenRequestDTO;
import com.munashechipanga.eharvest.dtos.response.RefreshTokenResponseDTO;
import com.munashechipanga.eharvest.dtos.request.VerificationRequestDTO;
import com.munashechipanga.eharvest.dtos.request.VerificationConfirmDTO;
import com.munashechipanga.eharvest.entities.RefreshToken;
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

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private VerificationService verificationService;

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

        RefreshToken refreshToken = refreshTokenService.create(user);
        return ResponseEntity.ok(new LoginResponse(token, refreshToken.getToken(), user.getRole(), user.getId()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequestDTO dto) {
        RefreshToken token = refreshTokenService.verify(dto.getRefreshToken());
        String newAccessToken = jwtUtil.generateToken(token.getUser());
        RefreshToken newRefresh = refreshTokenService.create(token.getUser());
        refreshTokenService.revoke(token.getToken());
        return ResponseEntity.ok(new RefreshTokenResponseDTO(newAccessToken, newRefresh.getToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequestDTO dto) {
        refreshTokenService.revoke(dto.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verification/request")
    public ResponseEntity<?> requestVerification(@RequestBody VerificationRequestDTO dto) {
        verificationService.requestVerification(dto.getUserId(), dto.getChannel());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verification/confirm")
    public ResponseEntity<?> confirmVerification(@RequestBody VerificationConfirmDTO dto) {
        verificationService.confirmVerification(dto.getUserId(), dto.getCode());
        return ResponseEntity.ok().build();
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
    private String refreshToken;
    private String role;
    private Long userId;

    public LoginResponse(String token, String refreshToken, String role, Long userId) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.role = role;
        this.userId = userId;
    }
}
