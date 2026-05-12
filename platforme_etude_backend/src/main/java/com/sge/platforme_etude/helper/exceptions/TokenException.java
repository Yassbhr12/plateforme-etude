package com.sge.platforme_etude.helper.exceptions;

import org.springframework.http.HttpStatus;

public class TokenException extends AppException {
    public TokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message, "TOKEN_ERROR");
    }
}
