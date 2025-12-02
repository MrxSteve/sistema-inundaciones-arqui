package com.inundaciones.sistema_inundaciones.services.auth;

import com.inundaciones.sistema_inundaciones.models.dto.request.GoogleTokenRequest;
import com.inundaciones.sistema_inundaciones.models.dto.request.LoginRequest;
import com.inundaciones.sistema_inundaciones.models.dto.request.UsuarioRequest;
import com.inundaciones.sistema_inundaciones.models.dto.response.AuthResponse;
import com.inundaciones.sistema_inundaciones.models.dto.response.UsuarioResponse;

public interface IAuthService {
    UsuarioResponse register(UsuarioRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse loginWithGoogle(GoogleTokenRequest request);
    void logout();
    UsuarioResponse getProfile();
    UsuarioResponse updateProfile(UsuarioRequest request);
}
