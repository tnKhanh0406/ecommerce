package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.Status;
import com.prj.ecommerce.dto.request.user.ChangePasswordRequest;
import com.prj.ecommerce.dto.request.user.LoginRequest;
import com.prj.ecommerce.dto.request.user.RegisterRequest;
import com.prj.ecommerce.dto.request.user.UpdateProfileRequest;
import com.prj.ecommerce.dto.response.user.UserResponse;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.exception.BadRequestException;
import com.prj.ecommerce.exception.ResourceAlreadyExistsException;
import com.prj.ecommerce.exception.UpdateResourceExistException;
import com.prj.ecommerce.repository.UserRepository;
import com.prj.ecommerce.service.JWTService;
import com.prj.ecommerce.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            throw new UpdateResourceExistException("Email already exists");
        }

        if (request.getPhoneNumber() != null &&
                userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), userEntity.getId())) {
            throw new UpdateResourceExistException("Phone number already exists");
        }
        userEntity.setFullName(request.getFullName());
        userEntity.setPhoneNumber(request.getPhoneNumber());
        userEntity.setEmail(request.getEmail());
        return UserResponse.fromEntity(userRepository.save(userEntity));
    }

    @Override
    public Page<UserResponse> getAllUsers(String search, Pageable pageable) {
        Page<UserEntity> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.searchUsers(search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(UserResponse::fromEntity);
    }

    @Override
    public UserResponse getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return UserResponse.fromEntity(user);
    }

    @Override
    public void updateUserStatus(Long userId, Status status) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
