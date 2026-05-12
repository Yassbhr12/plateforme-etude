package com.sge.platforme_etude.helper.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AppException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message, "UNAUTHORIZED");
    }
}

