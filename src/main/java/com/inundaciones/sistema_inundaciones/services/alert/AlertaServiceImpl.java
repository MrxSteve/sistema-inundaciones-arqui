package com.inundaciones.sistema_inundaciones.services.alert;

import com.inundaciones.sistema_inundaciones.models.dto.request.AlertaRequest;
import com.inundaciones.sistema_inundaciones.models.dto.response.AlertaResponse;
import com.inundaciones.sistema_inundaciones.models.entities.Alerta;
import com.inundaciones.sistema_inundaciones.models.entities.Usuario;
import com.inundaciones.sistema_inundaciones.models.enums.EstadoAlerta;
import com.inundaciones.sistema_inundaciones.models.enums.TipoAlerta;
import com.inundaciones.sistema_inundaciones.repositories.AlertaRepository;
import com.inundaciones.sistema_inundaciones.repositories.UsuarioRepository;
import com.inundaciones.sistema_inundaciones.services.email.EmailService;
import com.inundaciones.sistema_inundaciones.utils.mappers.AlertaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AlertaServiceImpl implements IAlertaService {
    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final AlertaMapper alertaMapper;

    @Override
    public AlertaResponse crearAlerta(AlertaRequest request) {
        log.info("Creando nueva alerta de tipo: {}", request.getTipo());

        Alerta alerta = alertaMapper.toEntity(request);

        alerta = alertaRepository.save(alerta);
        log.info("Alerta creada con ID: {}", alerta.getId());

        enviarEmailsAsync(alerta);

        return alertaMapper.toResponse(alerta);
    }

    @Override
    public AlertaResponse procesarAlertaEsp32(AlertaRequest request) {
        log.info("Procesando alerta desde ESP32 - Tipo: {}, Distancia: {} cm",
                request.getTipo(), request.getDistanciaDetectada());

        TipoAlerta tipoAlerta = determinarTipoAlerta(request.getDistanciaDetectada(), request.getTipo());
        request.setTipo(tipoAlerta);

        String mensajePersonalizado = generarMensaje(tipoAlerta, request.getDistanciaDetectada());
        request.setMensaje(mensajePersonalizado);

        return crearAlerta(request);
    }

    @Override
    public Page<AlertaResponse> obtenerAlertas(Pageable pageable) {
        Page<Alerta> alertas = alertaRepository.findAllByOrderByFechaCreacionDesc(pageable);
        return alertas.map(alertaMapper::toResponse);
    }

    @Override
    public Page<AlertaResponse> obtenerAlertasPorTipo(TipoAlerta tipo, Pageable pageable) {
        Page<Alerta> alertas = alertaRepository.findByTipoOrderByFechaCreacionDesc(tipo, pageable);
        return alertas.map(alertaMapper::toResponse);
    }

    @Override
    public AlertaResponse obtenerAlertaPorId(Long id) {
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada con ID: " + id));
        return alertaMapper.toResponse(alerta);
    }

    @Override
    public AlertaResponse resolverAlerta(Long id, String observaciones) {
        log.info("Resolviendo alerta ID: {} con observaciones: {}", id, observaciones);

        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada con ID: " + id));

        alerta.resolver(observaciones);
        alerta = alertaRepository.save(alerta);

        log.info("Alerta ID: {} resuelta exitosamente", id);
        return alertaMapper.toResponse(alerta);
    }

    @Override
    public List<AlertaResponse> obtenerAlertasActivas() {
        List<Alerta> alertasActivas = alertaRepository.findByEstadoOrderByFechaCreacionDesc(
                EstadoAlerta.ACTIVA);
        return alertasActivas.stream()
                .map(alertaMapper::toResponse)
                .toList();
    }

    @Override
    public List<AlertaResponse> obtenerAlertasResueltas() {
        List<Alerta> alertasResueltas = alertaRepository.findByEstadoOrderByFechaCreacionDesc(
                EstadoAlerta.RESUELTA);
        return alertasResueltas.stream()
                .map(alertaMapper::toResponse)
                .toList();
    }

    private void enviarEmailsAsync(Alerta alerta) {
        new Thread(() -> {
            try {
                log.info("Iniciando envío masivo de emails para alerta ID: {}", alerta.getId());

                List<Usuario> usuariosActivos = usuarioRepository.findByActivoTrue();
                log.info("Enviando emails a {} usuarios activos", usuariosActivos.size());

                int emailsEnviados = emailService.enviarAlertasMasivas(usuariosActivos, alerta);

                alerta.setEmailsEnviados(emailsEnviados);
                alertaRepository.save(alerta);

                log.info("Envío masivo completado. {} emails enviados de {} usuarios",
                        emailsEnviados, usuariosActivos.size());

            } catch (Exception e) {
                log.error("Error en envío masivo de emails para alerta ID: {}: {}",
                        alerta.getId(), e.getMessage());
            }
        }, "EmailSender-" + alerta.getId()).start();
    }

    private TipoAlerta determinarTipoAlerta(Float distancia, TipoAlerta tipoSugerido) {
        if (tipoSugerido != null) {
            return tipoSugerido;
        }

        if (distancia <= 20) {
            return TipoAlerta.ALERTA_ROJA;
        } else if (distancia <= 35) {
            return TipoAlerta.ALERTA_AMARILLA;
        } else {
            return TipoAlerta.SITUACION_NORMALIZADA;
        }
    }

    private String generarMensaje(TipoAlerta tipo, Float distancia) {
        return switch (tipo) {
            case ALERTA_ROJA ->
                String.format("ALERTA ROJA: Riesgo crítico de inundación detectado. Nivel de agua a %.1f cm. Evacue inmediatamente.", distancia);
            case ALERTA_AMARILLA ->
                String.format("ALERTA AMARILLA: Nivel de agua elevado detectado a %.1f cm. Manténgase alerta y tome precauciones.", distancia);
            case SITUACION_NORMALIZADA ->
                String.format("SITUACIÓN NORMALIZADA: El nivel de agua ha descendido a %.1f cm. La situación está bajo control.", distancia);
        };
    }
}
