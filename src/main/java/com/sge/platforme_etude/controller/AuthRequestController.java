package com.sge.platforme_etude.controller;


import com.sge.platforme_etude.dto.UserDto;
import com.sge.platforme_etude.dto.authentification.AuthRequest;
import com.sge.platforme_etude.dto.authentification.AuthResponse;
import com.sge.platforme_etude.dto.authentification.CreateUserRequest;
import com.sge.platforme_etude.dto.authentification.ValidationCodeRequest;
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

    public AuthRequestController(UserService service) {
        this.service = service;
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
    public ResponseEntity<?> validateCodeRequest(@Valid @RequestBody ValidationCodeRequest validationCodeRequest){
        try{
            String token = service.validateCode(validationCodeRequest);
            UserDto dto = service.findUserByEmail(validationCodeRequest.getEmail());
            AuthResponse authResponse = new AuthResponse(
                    token,
                    dto.getId(),
                    dto.getEmail(),
                    dto.getNom(),
                    dto.getPrenom(),
                    dto.getRole()
            );
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "Message", "Validation successful" , "Reponse" , authResponse
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                            "error" , e.getMessage()
                    )
            );
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
