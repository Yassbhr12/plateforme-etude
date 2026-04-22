package com.sge.platforme_etude.dto.authentification;

import com.sge.platforme_etude.dto.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @Valid
    @NotNull
    private UserDto user;

    @Valid
    @NotNull
    private AuthRequest auth;
}

