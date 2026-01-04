package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.dto.request.ChangePasswordRequest;
import com.prj.ecommerce.dto.request.LoginRequest;
import com.prj.ecommerce.dto.request.RegisterRequest;
import com.prj.ecommerce.dto.request.UpdateProfileRequest;
import com.prj.ecommerce.dto.response.UserResponse;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.exception.BadRequestException;
import com.prj.ecommerce.exception.ResourceAlreadyExistsException;
import com.prj.ecommerce.exception.UpdateResourceExistException;
import com.prj.ecommerce.repository.UserRepository;
import com.prj.ecommerce.service.JWTService;
import com.prj.ecommerce.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private UserEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

//    @Override
//    public String verifyUser(LoginRequest loginRequest) {
//        Authentication authentication = authenticationManager
//                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
//        if (authentication.isAuthenticated()) {
//            return jwtService.generateToken(loginRequest.getUsername());
//        }
//        return "fail";
//    }
    @Override
    public String verifyUser(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        return jwtService.generateToken(userDetails);
    }


    @Override
    public UserResponse registerUser(RegisterRequest registerRequest) {
        UserEntity userEntity = new UserEntity();
        if (registerRequest.getUsername() != null && userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }

        if (registerRequest.getEmail() != null && userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        if (registerRequest.getPhoneNumber() != null && userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            throw new ResourceAlreadyExistsException("Phone number already exists");
        }
        userEntity.setUsername(registerRequest.getUsername());
        userEntity.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userEntity.setFullName(registerRequest.getFullName());
        userEntity.setPhoneNumber(registerRequest.getPhoneNumber());
        userEntity.setEmail(registerRequest.getEmail());
        return UserResponse.fromEntity(userRepository.save(userEntity));
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        UserEntity userEntity = getCurrentUser();
        if (!passwordEncoder.matches(changePasswordRequest.getPassword(), userEntity.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        if (changePasswordRequest.getPassword().equals(changePasswordRequest.getNewPassword())) {
            throw new BadRequestException("New password cannot be the same as old password");
        }
        userEntity.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(userEntity);
    }

    @Override
    public UserResponse updateUser(UpdateProfileRequest request) {
        UserEntity userEntity = getCurrentUser();
        if (request.getEmail() != null &&
                userRepository.existsByEmailAndIdNot(request.getEmail(), userEntity.getId())) {
//            throw new ResourceAlreadyExistsException("Email already exists");
            throw new UpdateResourceExistException("Email already exists");
        }

        if (request.getPhoneNumber() != null &&
                userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), userEntity.getId())) {
//            throw new ResourceAlreadyExistsException("Phone number already exists");
            throw new UpdateResourceExistException("Phone number already exists");
        }
        userEntity.setFullName(request.getFullName());
        userEntity.setPhoneNumber(request.getPhoneNumber());
        userEntity.setEmail(request.getEmail());
        return UserResponse.fromEntity(userRepository.save(userEntity));
    }
}
