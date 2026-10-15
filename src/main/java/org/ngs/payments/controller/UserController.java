package org.ngs.payments.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngs.payments.dto.request.UserCreateRequest;
import org.ngs.payments.dto.request.UserLoginRequest;
import org.ngs.payments.dto.response.UserCreateResponse;
import org.ngs.payments.dto.response.UserLoginResponse;
import org.ngs.payments.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserCreateResponse> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        log.info("user create request {}", userCreateRequest);
        UserCreateResponse response = userService.createUser(userCreateRequest);
        log.info("user create response {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/sessions")
    public ResponseEntity<UserLoginResponse> loginUser(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        log.info("user login request {}", userLoginRequest);
        UserLoginResponse response = userService.loginUser(userLoginRequest);
        return ResponseEntity.ok(response);
    }
}
