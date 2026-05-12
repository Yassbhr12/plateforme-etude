package com.sge.platforme_etude.helper.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends AppException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message, "CONFLICT");
    }
}

