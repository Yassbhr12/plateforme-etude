package com.sge.platforme_etude.helper.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends AppException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message, "BAD_REQUEST");
    }
}

