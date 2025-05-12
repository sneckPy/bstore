package com.bstore.commons.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DataRetrievalException extends RuntimeException{
    public DataRetrievalException(String message) {
        super(message);
    }
}
