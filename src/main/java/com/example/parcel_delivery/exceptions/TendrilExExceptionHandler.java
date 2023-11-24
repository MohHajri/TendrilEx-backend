package com.example.parcel_delivery.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class TendrilExExceptionHandler extends RuntimeException {

    private HttpStatus status;

    public TendrilExExceptionHandler(String message) {
        super(message);
    }

    public TendrilExExceptionHandler(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
