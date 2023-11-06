package com.example.parcel_delivery.exceptions;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.example.parcel_delivery.models.dtos.responses.ErrorRes;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalException extends ResponseEntityExceptionHandler {

    /**
     * Handles validation errors from incoming requests.
     * Returns a map of field errors.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ErrorRes ErrorRes = new ErrorRes();
        ErrorRes.setStatus(HttpStatus.BAD_REQUEST.value());
        ErrorRes.setMessage(errors.toString()); 

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);

    }

    /**
     * Handles custom APIException.
     * Returns a structured ErrorRes.
     */
    @ExceptionHandler(PrcelDeliveryExceptionHandler.class)
    public ResponseEntity<ErrorRes> handleAPIException(PrcelDeliveryExceptionHandler ex) {
        ErrorRes ErrorRes = new ErrorRes();
        ErrorRes.setStatus(ex.getStatus().value());
        // ErrorRes.setMessage(ex.getMessage());
        ErrorRes.setMessage(ex.getMessage());

        return new ResponseEntity<>(ErrorRes, ex.getStatus());

    }

    /**
     * Handles all other uncaught exceptions.
     * Returns a generic ErrorRes.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRes> handleGlobalException(Exception ex) {
        ErrorRes ErrorRes = new ErrorRes();
        ErrorRes.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        // ErrorRes.setMessage(ex.getMessage());
        ErrorRes.setMessage(ex.getMessage());

        return new ResponseEntity<>(ErrorRes, HttpStatus.INTERNAL_SERVER_ERROR);

    }
}