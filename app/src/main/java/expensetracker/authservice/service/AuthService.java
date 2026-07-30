package expensetracker.authservice.service;

import lombok.AllArgsConstructor;
import expensetracker.authservice.dto.request.AuthRequestDTO;
import expensetracker.authservice.dto.request.CreateUserDTO;
import expensetracker.authservice.dto.request.RefreshTokenRequestDTO;
import expensetracker.authservice.dto.response.AuthResponseDTO;
import expensetracker.authservice.entities.RefreshToken;
import expensetracker.authservice.entities.UserInfo;
import expensetracker.authservice.events.AuthUserRegisteredEvent;
import expensetracker.authservice.kafka.AuthEventProducer;
import expensetracker.authservice.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthEventProducer authEventProducer;

    public AuthResponseDTO signup(CreateUserDTO createUserDTO) throws RuntimeException {
//        1. check if user exist
        if(userRepository.existsByUsername(createUserDTO.getUsername())){
            throw new RuntimeException("Username already exists: " + createUserDTO.getUsername());
        }

        if(userRepository.existsByEmail(createUserDTO.getEmail())){
            throw new RuntimeException("Email already exists: " + createUserDTO.getEmail());
        }

//        2. Create new user and save user
        UserInfo user = new UserInfo();
        user.setUsername(createUserDTO.getUsername());
        user.setEmail(createUserDTO.getEmail());
        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));
        user.setRoles(new HashSet<>());

        UserInfo savedUser = userRepository.save(user);

//        3. Create kafka event and send event
        AuthUserRegisteredEvent event = new AuthUserRegisteredEvent();
        event.setUserId(savedUser.getUserId());
        event.setUsername(createUserDTO.getUsername());
        event.setEmail(createUserDTO.getEmail());
        event.setFirstName(createUserDTO.getFirstName());
        event.setLastName(createUserDTO.getLastName());

        authEventProducer.sendUserRegisteredEvent(event);

//        4.Generate tokens
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getUserId());
        String accessToken = jwtService.generateToken(savedUser.getUserId());

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

//        2. Get userId
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails != null ? userDetails.getUserId() : null;


//        3. Generate tokens
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId);
        String accessToken = jwtService.generateToken(userId);

//        4. return response
        return AuthResponseDTO
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO) throws RuntimeException{
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
                    String accessToken = jwtService.generateToken(userInfo.getUserId());
                    return AuthResponseDTO
                            .builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshTokenRequestDTO.getToken())
                            .build();
                }).orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }
}
