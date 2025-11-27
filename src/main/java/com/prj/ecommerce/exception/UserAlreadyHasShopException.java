package com.prj.ecommerce.exception;

public class UserAlreadyHasShopException extends RuntimeException {
    public UserAlreadyHasShopException(String message) {
        super(message);
    }
}
