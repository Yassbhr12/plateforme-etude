package com.sge.platforme_etude.controller;


import com.sge.platforme_etude.dto.UserDto;
import com.sge.platforme_etude.dto.authentification.*;
import com.sge.platforme_etude.entite.RefreshToken;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.security.jwt.JwtUtils;
import com.sge.platforme_etude.service.user.RefreshTokenService;
import com.sge.platforme_etude.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRequestController {

    private final UserService service;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;

    public AuthRequestController(UserService service,
                                 RefreshTokenService refreshTokenService,
                                 JwtUtils jwtUtils) {
        this.service = service;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request){
        UserDto dto = service.createUser(request.getUser(), request.getAuth());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login (@Valid @RequestBody AuthRequest authRequest){
        try {
            service.loginProcess(authRequest);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message" , "User successfully logged in. Validation code sent.",
                    "login", authRequest.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error : " , e.getMessage()
            ));
        }
    }

    @PostMapping("/login/validation")
    public ResponseEntity<?> validateCodeRequest(@Valid @RequestBody ValidationCodeRequest validationCodeRequest) {
        try {
            String[] tokens = service.validateCode(validationCodeRequest);
            String accessToken  = tokens[0];
            String refreshToken = tokens[1];

            UserDto dto = service.findUserByEmail(validationCodeRequest.getEmail());
            AuthResponse authResponse = new AuthResponse(
                    accessToken, refreshToken,
                    dto.getId(), dto.getEmail(),
                    dto.getNom(), dto.getPrenom(), dto.getRole()
            );
            return ResponseEntity.ok(Map.of(
                    "message", "Validation successful",
                    "response", authResponse
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            RefreshToken oldToken = refreshTokenService.verifyAndGet(request.getRefreshToken());
            RefreshToken newRefreshToken = refreshTokenService.rotate(oldToken);

            User user = newRefreshToken.getUser();
            String newAccessToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name());

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", newRefreshToken.getToken(),
                    "type", "Bearer"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            RefreshToken token = refreshTokenService.verifyAndGet(request.getRefreshToken());
            refreshTokenService.revokeAllUserTokens(token.getUser());
            return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
//    @GetMapping("/me")
//    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
//        if (authentication == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
//                    "error", "Non authentifié"
//            ));
//        }
//
//        String login = authentication.getName();
//        UserDto dto = service.findUserByEmail(login);
//
//        return ResponseEntity.ok(dto);
//    }
}
