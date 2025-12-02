package com.inundaciones.sistema_inundaciones.utils.seeder;

import com.inundaciones.sistema_inundaciones.models.entities.Rol;
import com.inundaciones.sistema_inundaciones.models.entities.Usuario;
import com.inundaciones.sistema_inundaciones.repositories.RolRepository;
import com.inundaciones.sistema_inundaciones.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;
    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedAdminUser();
    }

    private void seedRoles() {
        log.info("Iniciando seed de roles...");

        if (!rolRepository.existsByNombreIgnoreCase("ADMIN")) {
            Rol adminRole = Rol.builder()
                    .nombre("ADMIN")
                    .build();
            rolRepository.save(adminRole);
            log.info("Rol ADMIN creado exitosamente");
        } else {
            log.info("Rol ADMIN ya existe, omitiendo creación");
        }

        if (!rolRepository.existsByNombreIgnoreCase("USUARIO")) {
            Rol clienteRole = Rol.builder()
                    .nombre("USUARIO")
                    .build();
            rolRepository.save(clienteRole);
            log.info("Rol USUARIO creado exitosamente");
        } else {
            log.info("Rol USUARIO ya existe, omitiendo creación");
        }

        log.info("Seed de roles completado");
    }

    private void seedAdminUser() {
        log.info("Iniciando seed de users...");

        Rol rolAdmin = rolRepository.findByNombreIgnoreCase("ADMIN")
                .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));

        if (!usuarioRepository.existsByEmail(adminEmail)) {
            Usuario admin = Usuario.builder()
                    .nombre("Administrador")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .telefono("000000000")
                    .activo(true)
                    .roles(List.of(rolAdmin))
                    .createdAt(LocalDateTime.now())
                    .build();
            usuarioRepository.save(admin);
            log.info("Usuario ADMIN: {} creado correctamente", admin.getEmail());
        } else {
            log.info("Usuario ADMIN: {} ya existe", adminEmail);
        }
    }
}
