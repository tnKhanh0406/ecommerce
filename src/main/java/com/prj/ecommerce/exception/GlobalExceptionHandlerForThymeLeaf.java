package com.prj.ecommerce.exception;

import com.prj.ecommerce.dto.request.RegisterRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandlerForThymeLeaf {
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public String handleResourceExists(ResourceAlreadyExistsException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }
}
