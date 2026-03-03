package expensetracker.authservice.controller;

import lombok.Data;
import expensetracker.authservice.dto.request.AuthRequestDTO;
import expensetracker.authservice.dto.request.CreateUserDTO;
import expensetracker.authservice.dto.request.RefreshTokenRequestDTO;
import expensetracker.authservice.dto.response.AuthResponseDTO;
import expensetracker.authservice.service.AuthService;
import expensetracker.authservice.service.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@Data
@RestController
@RequestMapping("/auth/v1")
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService,  RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> SignUp(@RequestBody CreateUserDTO createUserDTO) {
        AuthResponseDTO response = authService.signup(createUserDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> Login(@RequestBody AuthRequestDTO authRequestDTO) {
        try {
            AuthResponseDTO response = authService.login(authRequestDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> RefreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        try {
            AuthResponseDTO responseDTO = authService.refreshToken(refreshTokenRequestDTO);
            return  new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Refresh token not found" + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        boolean deleted = refreshTokenService.deleteByToken(refreshTokenRequestDTO.getToken());

        Map<String, String> response = new HashMap<>();
        if (deleted) {
            response.put("message", "Successfully logged out");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Refresh token not found (already logged out?)");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
