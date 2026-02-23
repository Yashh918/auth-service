package org.example.service;

import lombok.AllArgsConstructor;
import org.example.dto.request.AuthRequestDTO;
import org.example.dto.request.CreateUserDTO;
import org.example.dto.request.RefreshTokenRequestDTO;
import org.example.dto.response.AuthResponseDTO;
import org.example.entities.RefreshToken;
import org.example.entities.UserInfo;
import org.example.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDTO signup(CreateUserDTO createUserDTO) throws RuntimeException {
//        1. check if user exist
        if(userRepository.existsByUsername(createUserDTO.getUsername())){
            throw new RuntimeException("Username already exists: " + createUserDTO.getUsername());
        }

        if(userRepository.existsByEmail(createUserDTO.getEmail())){
            throw new RuntimeException("Email already exists: " + createUserDTO.getEmail());
        }

//        2. Create new user
        UserInfo user = new UserInfo();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(createUserDTO.getUsername());
        user.setEmail(createUserDTO.getEmail());
        user.setFirstName(createUserDTO.getFirstName());
        user.setLastName(createUserDTO.getLastName());
        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));
        user.setRoles(new HashSet<>());

//        3. Save user
        UserInfo savedUser = userRepository.save(user);

//        4.Generate tokens
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getUsername());
        String accessToken = jwtService.generateToken(savedUser.getUsername());

//        4. Return response
        return AuthResponseDTO
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public AuthResponseDTO login(AuthRequestDTO authRequestDTO) {
//        1. Verify if the password matches the usernames password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequestDTO.getUsername(),
                        authRequestDTO.getPassword()
                )
        );

        System.out.println("Authentication successful");


//        2. Generate tokens
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
        String accessToken = jwtService.generateToken(authRequestDTO.getUsername());

//        3. return response
        return AuthResponseDTO
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO) {
//        1. Find the refresh token in DB
//        2. Verify its expiration
//        3. Fetch user details
//        4. Generate access token
//        5. Return both access token and refresh token

        return refreshTokenService
                .findByToken(refreshTokenRequestDTO.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(userInfo -> {
                    String accessToken = jwtService.generateToken(userInfo.getUsername());
                    return AuthResponseDTO
                            .builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshTokenRequestDTO.getToken())
                            .build();
                }).orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }
}
