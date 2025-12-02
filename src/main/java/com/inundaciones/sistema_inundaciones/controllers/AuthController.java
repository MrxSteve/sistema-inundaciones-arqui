package com.inundaciones.sistema_inundaciones.controllers;

import com.inundaciones.sistema_inundaciones.models.dto.request.GoogleTokenRequest;
import com.inundaciones.sistema_inundaciones.models.dto.request.LoginRequest;
import com.inundaciones.sistema_inundaciones.models.dto.request.UsuarioRequest;
import com.inundaciones.sistema_inundaciones.models.dto.response.AuthResponse;
import com.inundaciones.sistema_inundaciones.models.dto.response.UsuarioResponse;
import com.inundaciones.sistema_inundaciones.services.auth.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación y autorización")
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión con email y contraseña")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    @Operation(summary = "Iniciar sesión con Google OAuth2")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleTokenRequest request) {
        AuthResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile")
    @Operation(summary = "Obtener perfil del usuario autenticado")
    public ResponseEntity<UsuarioResponse> getProfile() {
        UsuarioResponse response = authService.getProfile();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @Operation(summary = "Actualizar perfil del usuario")
    public ResponseEntity<UsuarioResponse> updateProfile(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = authService.updateProfile(request);
        return ResponseEntity.ok(response);
    }
}
