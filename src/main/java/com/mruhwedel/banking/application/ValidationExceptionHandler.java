package com.mruhwedel.banking.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;

@Slf4j
@ControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler({
            ConstraintViolationException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<String> handle(Throwable ex) {
        log.info(ex.getLocalizedMessage());
        return ResponseEntity
                .badRequest()
                .body('"' + ex.getLocalizedMessage() + '"');
    }

}
