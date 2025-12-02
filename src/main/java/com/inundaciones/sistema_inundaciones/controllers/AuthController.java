package com.inundaciones.sistema_inundaciones.controllers;

import com.inundaciones.sistema_inundaciones.models.dto.request.LoginRequest;
import com.inundaciones.sistema_inundaciones.models.dto.request.UsuarioRequest;
import com.inundaciones.sistema_inundaciones.models.dto.response.AuthResponse;
import com.inundaciones.sistema_inundaciones.models.dto.response.UsuarioResponse;
import com.inundaciones.sistema_inundaciones.services.auth.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse usuarioResponse = authService.register(request);
        return ResponseEntity.ok(usuarioResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UsuarioResponse> getProfile() {
        UsuarioResponse usuarioResponse = authService.getProfile();
        return ResponseEntity.ok(usuarioResponse);
    }

    @PutMapping("/profile")
    public ResponseEntity<UsuarioResponse> updateProfile(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse updatedProfile = authService.updateProfile(request);
        return ResponseEntity.ok(updatedProfile);
    }
}
