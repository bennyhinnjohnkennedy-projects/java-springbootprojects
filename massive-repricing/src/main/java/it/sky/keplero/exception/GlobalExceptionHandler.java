package it.sky.keplero.exception;

import it.sky.keplero.DTO.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Generic Exception occured: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("1", e.getMessage()));
    }
}
