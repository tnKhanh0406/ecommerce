package com.prj.ecommerce.exception;

public class UpdateResourceExistException extends RuntimeException {
    public UpdateResourceExistException(String message) {
        super(message);
    }
}
