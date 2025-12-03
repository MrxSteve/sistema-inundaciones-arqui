package com.inundaciones.sistema_inundaciones.services.esp32;

import com.inundaciones.sistema_inundaciones.models.entities.Usuario;
import com.inundaciones.sistema_inundaciones.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ESP32Service {
    private final UsuarioRepository usuarioRepository;

    public Map<String, Object> obtenerNumerosSMS() {
        log.info("ESP32 solicitando números de teléfono para SMS");

        try {
            List<Usuario> usuariosConTelefono = usuarioRepository.findByActivoTrueAndTelefonoIsNotNull();

            List<String> numeros = usuariosConTelefono.stream()
                    .map(Usuario::getTelefono)
                    .filter(telefono -> telefono != null && !telefono.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            log.info("Encontrados {} números de teléfono para SMS", numeros.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", numeros.size());
            response.put("numeros", numeros);
            response.put("timestamp", LocalDateTime.now().toString());

            return response;

        } catch (Exception e) {
            log.error("Error obteniendo números SMS: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("numeros", new ArrayList<>());
            response.put("total", 0);
            return response;
        }
    }
}

