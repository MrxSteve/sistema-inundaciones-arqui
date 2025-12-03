package com.inundaciones.sistema_inundaciones.controllers;

import com.inundaciones.sistema_inundaciones.models.dto.request.AlertaRequest;
import com.inundaciones.sistema_inundaciones.models.dto.response.AlertaResponse;
import com.inundaciones.sistema_inundaciones.models.enums.TipoAlerta;
import com.inundaciones.sistema_inundaciones.services.alert.IAlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Alertas", description = "API para gestión de alertas de inundación")
public class AlertaController {
    private final IAlertaService alertaService;

    @PostMapping("/esp32")
    @Operation(summary = "Recibir alerta desde ESP32",
               description = "Endpoint para que el ESP32 envíe datos de alertas de inundación")
    public ResponseEntity<AlertaResponse> recibirAlertaEsp32(@Valid @RequestBody AlertaRequest request) {
        log.info("Recibiendo alerta desde ESP32 - Distancia: {} cm, Dispositivo: {}",
                request.getDistanciaDetectada(), request.getDispositivoId());

        AlertaResponse response = alertaService.procesarAlertaEsp32(request);

        log.info("Alerta procesada exitosamente - ID: {}, Emails enviados: {}",
                response.getId(), response.getEmailsEnviados());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear alerta manual",
               description = "Permite a los administradores crear alertas manualmente")
    public ResponseEntity<AlertaResponse> crearAlerta(@Valid @RequestBody AlertaRequest request) {
        log.info("Creando alerta manual por administrador - Tipo: {}", request.getTipo());

        AlertaResponse response = alertaService.crearAlerta(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/todas")
    @Operation(summary = "Obtener todas las alertas",
               description = "Lista paginada de todas las alertas del sistema")
    public ResponseEntity<Page<AlertaResponse>> obtenerAlertas(Pageable pageable) {
        Page<AlertaResponse> alertas = alertaService.obtenerAlertas(pageable);
        return ResponseEntity.ok(alertas);
    }

    @GetMapping("/public/tipo/{tipo}")
    @Operation(summary = "Obtener alertas por tipo",
               description = "Lista paginada de alertas filtradas por tipo")
    public ResponseEntity<Page<AlertaResponse>> obtenerAlertasPorTipo(
            @PathVariable TipoAlerta tipo,
            Pageable pageable) {

        Page<AlertaResponse> alertas = alertaService.obtenerAlertasPorTipo(tipo, pageable);
        return ResponseEntity.ok(alertas);
    }

    @GetMapping("/public/activas")
    @Operation(summary = "Obtener alertas activas",
               description = "Lista de todas las alertas que están actualmente activas")
    public ResponseEntity<List<AlertaResponse>> obtenerAlertasActivas() {
        List<AlertaResponse> alertasActivas = alertaService.obtenerAlertasActivas();
        return ResponseEntity.ok(alertasActivas);
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "Obtener alerta por ID",
               description = "Obtiene los detalles de una alerta específica")
    public ResponseEntity<AlertaResponse> obtenerAlertaPorId(@PathVariable Long id) {
        AlertaResponse alerta = alertaService.obtenerAlertaPorId(id);
        return ResponseEntity.ok(alerta);
    }

    @PutMapping("/{id}/resolver")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resolver alerta",
               description = "Marca una alerta como resuelta con observaciones")
    public ResponseEntity<AlertaResponse> resolverAlerta(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String observaciones = body.getOrDefault("observaciones", "Situación resuelta por administrador");
        AlertaResponse alerta = alertaService.resolverAlerta(id, observaciones);

        log.info("Alerta ID: {} resuelta por administrador", id);
        return ResponseEntity.ok(alerta);
    }

    @GetMapping("/test-email")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Test de envío de email",
               description = "Endpoint de prueba para verificar el envío de emails")
    public ResponseEntity<Map<String, String>> testEmail() {
        // Crear una alerta de prueba
        AlertaRequest testRequest = new AlertaRequest();
        testRequest.setTipo(TipoAlerta.ALERTA_AMARILLA);
        testRequest.setMensaje("Esta es una alerta de prueba para verificar el sistema de emails");
        testRequest.setDistanciaDetectada(25.0f);
        testRequest.setUbicacion("Ubicación de prueba");
        testRequest.setDispositivoId("TEST-ESP32");

        AlertaResponse response = alertaService.crearAlerta(testRequest);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Email de prueba enviado",
                "alertaId", response.getId().toString(),
                "emailsEnviados", response.getEmailsEnviados().toString()
        ));
    }
}
