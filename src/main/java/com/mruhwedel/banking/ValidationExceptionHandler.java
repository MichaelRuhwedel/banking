package com.mruhwedel.banking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@ControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    public ResponseEntity<String> resourceNotFoundException(ConstraintViolationException ex) {
        log.info(ex.getLocalizedMessage());
        return ResponseEntity.badRequest()
                .body(ex.getLocalizedMessage());
    }
}
