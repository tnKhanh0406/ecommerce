package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.LoginRequest;
import com.prj.ecommerce.dto.request.RegisterRequest;
import com.prj.ecommerce.dto.response.UserResponse;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public String index() {
        return "Hello World";
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.verifyUser(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody RegisterRequest registerRequest) {
        UserEntity user = userService.registerUser(registerRequest);
        UserResponse res = new UserResponse().toUserResponse(user);
        return ResponseEntity.ok(res);
    }
}
