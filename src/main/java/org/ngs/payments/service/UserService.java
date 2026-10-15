package org.ngs.payments.service;

import lombok.RequiredArgsConstructor;
import org.ngs.payments.dto.request.UserCreateRequest;
import org.ngs.payments.dto.request.UserLoginRequest;
import org.ngs.payments.dto.response.UserCreateResponse;
import org.ngs.payments.dto.response.UserLoginResponse;
import org.ngs.payments.entity.UserEntity;
import org.ngs.payments.exception.EmailAlreadyExistsException;
import org.ngs.payments.exception.InvalidCredentialsException;
import org.ngs.payments.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public UserCreateResponse createUser(UserCreateRequest userCreateRequest) {
        String email = userCreateRequest.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        UserEntity userEntity = new UserEntity(email, userCreateRequest.getUserType(),
                passwordEncoder.encode(userCreateRequest.getPassword()));
        userRepository.save(userEntity);
        return new UserCreateResponse(userEntity.getId(), userEntity.getEmail(), userEntity.getUserType());
    }

    public UserLoginResponse loginUser(UserLoginRequest userLoginRequest) {
        String email = userLoginRequest.getEmail().trim().toLowerCase();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(userLoginRequest.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        String token = jwtService.generate(user);
        return new UserLoginResponse(user.getId(), user.getEmail(), user.getUserType(), token);
    }
}
