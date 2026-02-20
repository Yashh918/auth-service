package org.example.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.dto.request.CreateUserDTO;
import org.example.dto.response.AuthResponseDTO;
import org.example.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Data
@RestController
@RequestMapping("/auth/v1")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> SignUp(@RequestBody CreateUserDTO createUserDTO) {
        AuthResponseDTO response = authService.signup(createUserDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
