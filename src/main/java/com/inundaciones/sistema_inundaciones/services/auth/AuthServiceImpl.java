package com.inundaciones.sistema_inundaciones.services.auth;

import com.inundaciones.sistema_inundaciones.models.dto.request.GoogleTokenRequest;
import com.inundaciones.sistema_inundaciones.models.dto.request.LoginRequest;
import com.inundaciones.sistema_inundaciones.models.dto.request.UsuarioRequest;
import com.inundaciones.sistema_inundaciones.models.dto.response.AuthResponse;
import com.inundaciones.sistema_inundaciones.models.dto.response.UsuarioResponse;
import com.inundaciones.sistema_inundaciones.models.entities.Rol;
import com.inundaciones.sistema_inundaciones.models.entities.Usuario;
import com.inundaciones.sistema_inundaciones.models.enums.TipoNotificacion;
import com.inundaciones.sistema_inundaciones.repositories.UsuarioRepository;
import com.inundaciones.sistema_inundaciones.security.JwtService;
import com.inundaciones.sistema_inundaciones.utils.mappers.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final UsuarioRepository usuarioRepository;
    private final IRolService rolService;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final IUserService userService;
    private final GoogleOAuthService googleOAuthService;

    @Override
    public UsuarioResponse register(UsuarioRequest request) {
        Usuario usuario = usuarioMapper.toEntity(request);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setActivo(true);

        List<Rol> roles = new ArrayList<>();
        roles.add(rolService.buscarEntidadPorNombre("USUARIO"));

        usuario.setRoles(roles);
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(usuarioGuardado);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        String jwtToken = jwtService.generateToken(userDetails);

        UsuarioResponse usuario = userService.buscarPorEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario con email: " + request.getEmail() + " no encontrado después de la autenticación"));

        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .usuario(usuario)
                .build();
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Override
    public UsuarioResponse getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario con email: " + email + " no encontrado"));
    }

    @Override
    public UsuarioResponse updateProfile(UsuarioRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario con email: " + email + " no encontrado"));

        String passActual = usuario.getPassword();

        usuarioMapper.updateEntity(request, usuario);

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        } else {
            usuario.setPassword(passActual);
        }

        Usuario actualizado = usuarioRepository.save(usuario);

        return usuarioMapper.toResponse(actualizado);
    }

    @Override
    public AuthResponse loginWithGoogle(GoogleTokenRequest request) {
        try {
            var payload = googleOAuthService.verifyGoogleToken(request.getToken());

            String email = payload.getEmail();
            String nombre = (String) payload.get("name");
            String googleId = payload.getSubject();
            String avatarUrl = (String) payload.get("picture");

            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElse(usuarioRepository.findByGoogleId(googleId).orElse(null));

            if (usuario == null) {
                List<Rol> roles = new ArrayList<>();
                roles.add(rolService.buscarEntidadPorNombre("USUARIO"));

                usuario = Usuario.builder()
                        .nombre(nombre)
                        .email(email)
                        .googleId(googleId)
                        .avatarUrl(avatarUrl)
                        .activo(true)
                        .roles(roles)
                        .tipoNotificacion(TipoNotificacion.AMBOS)
                        .createdAt(LocalDateTime.now())
                        .build();

                usuario = usuarioRepository.save(usuario);
            } else {
                boolean needsUpdate = false;
                if (usuario.getGoogleId() == null) {
                    usuario.setGoogleId(googleId);
                    needsUpdate = true;
                }
                if (avatarUrl != null && !avatarUrl.equals(usuario.getAvatarUrl())) {
                    usuario.setAvatarUrl(avatarUrl);
                    needsUpdate = true;
                }
                if (needsUpdate) {
                    usuario = usuarioRepository.save(usuario);
                }
            }

            UserDetails userDetails = new User(
                    usuario.getEmail(),
                    "",
                    usuario.getActivo(),
                    true,
                    true,
                    true,
                    usuario.getRoles().stream()
                            .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre()))
                            .collect(java.util.stream.Collectors.toList())
            );

            String jwtToken = jwtService.generateToken(userDetails);

            UsuarioResponse usuarioResponse = usuarioMapper.toResponse(usuario);

            return AuthResponse.builder()
                    .token(jwtToken)
                    .tokenType("Bearer")
                    .usuario(usuarioResponse)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error al autenticar con Google: " + e.getMessage());
        }
    }

}
