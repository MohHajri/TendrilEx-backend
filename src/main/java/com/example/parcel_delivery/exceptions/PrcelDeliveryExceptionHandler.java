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
public class PrcelDeliveryExceptionHandler extends RuntimeException {

    private HttpStatus status;

    public PrcelDeliveryExceptionHandler(String message) {
        super(message);
    }

    public PrcelDeliveryExceptionHandler(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
