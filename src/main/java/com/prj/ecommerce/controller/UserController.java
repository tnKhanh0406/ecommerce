package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.LoginDTO;
import com.prj.ecommerce.dto.RegisterDTO;
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
    public ResponseEntity<?> loginUser(@RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(userService.verifyUser(loginDTO));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterDTO registerDTO) {
        UserEntity user = userService.registerUser(registerDTO);
        RegisterDTO res = new RegisterDTO();
        res.setUsername(user.getUsername());
        res.setFullName(user.getFullName());
        res.setPassword(user.getPassword());
        return ResponseEntity.ok(res);
    }
}
