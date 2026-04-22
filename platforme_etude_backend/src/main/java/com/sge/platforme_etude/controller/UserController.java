package com.sge.platforme_etude.controller;

import com.sge.platforme_etude.dto.UserDto;
import com.sge.platforme_etude.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/me", "/api/profil"})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails){
        UserDto dto = userService.findUserByEmail(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PutMapping
    public ResponseEntity<UserDto> updateUserById(@Valid @RequestBody UserDto userDto , @AuthenticationPrincipal UserDetails details){
        UserDto userDto1 = userService.findUserByEmail(details.getUsername());
        UserDto dto = userService.updateUserById(userDto,userDto1.getId());
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

}
