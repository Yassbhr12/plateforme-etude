package com.sge.platforme_etude.helper.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionMappingTest {

    @Test
    void badRequestException_hasExpectedStatusAndCode() {
        BadRequestException ex = new BadRequestException("invalid");
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("BAD_REQUEST", ex.getErrorCode());
    }

    @Test
    void notFoundException_hasExpectedStatusAndCode() {
        NotFoundException ex = new NotFoundException("missing");
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void unauthorizedException_hasExpectedStatusAndCode() {
        UnauthorizedException ex = new UnauthorizedException("nope");
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("UNAUTHORIZED", ex.getErrorCode());
    }

    @Test
    void forbiddenException_hasExpectedStatusAndCode() {
        ForbiddenException ex = new ForbiddenException("nope");
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("FORBIDDEN", ex.getErrorCode());
    }

    @Test
    void conflictException_hasExpectedStatusAndCode() {
        ConflictException ex = new ConflictException("dup");
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("CONFLICT", ex.getErrorCode());
    }

    @Test
    void tokenException_hasExpectedStatusAndCode() {
        TokenException ex = new TokenException("token");
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("TOKEN_ERROR", ex.getErrorCode());
    }
}

