package com.sge.platforme_etude.helper.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends AppException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message, "FORBIDDEN");
    }
}

