package com.inundaciones.sistema_inundaciones.services.email;

import com.inundaciones.sistema_inundaciones.models.entities.Alerta;
import com.inundaciones.sistema_inundaciones.models.entities.Usuario;
import com.inundaciones.sistema_inundaciones.models.enums.TipoAlerta;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;
    @Value("${spring.application.name}")
    private String appName;

    public boolean enviarAlertaInundacion(Usuario usuario, Alerta alerta) {
        try {
            log.debug("Iniciando envío de email a: {} para alerta tipo: {}", usuario.getEmail(), alerta.getTipo());

            if (fromEmail == null || fromEmail.isEmpty()) {
                log.error("EMAIL_USERNAME no está configurado en las variables de entorno");
                return false;
            }

            if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
                log.error("Usuario {} no tiene email configurado", usuario.getNombre());
                return false;
            }

            String subject = generarSubject(alerta.getTipo());
            String htmlContent = generarContenidoAlerta(usuario, alerta);

            log.debug("Enviando email desde: {} hacia: {} con asunto: {}", fromEmail, usuario.getEmail(), subject);
            enviarEmail(usuario.getEmail(), subject, htmlContent);
            log.info("Email de alerta enviado exitosamente a: {}", usuario.getEmail());
            return true;

        } catch (MessagingException e) {
            log.error("Error de mensajería enviando email a {}: {}", usuario.getEmail(), e.getMessage());
            log.debug("Stack trace completo: ", e);
            return false;
        } catch (Exception e) {
            log.error("Error inesperado enviando email a {}: {}", usuario.getEmail(), e.getMessage());
            log.debug("Stack trace completo: ", e);
            return false;
        }
    }

    public int enviarAlertasMasivas(List<Usuario> usuarios, Alerta alerta) {
        int emailsEnviados = 0;
        int emailsConErrores = 0;

        log.info("Iniciando envío masivo a {} usuarios para alerta tipo: {}", usuarios.size(), alerta.getTipo());

        if (fromEmail == null || fromEmail.isEmpty()) {
            log.error("No se puede enviar emails: EMAIL_USERNAME no configurado");
            return 0;
        }

        for (Usuario usuario : usuarios) {
            try {
                if (enviarAlertaInundacion(usuario, alerta)) {
                    emailsEnviados++;
                } else {
                    emailsConErrores++;
                }

                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("Proceso de envío interrumpido");
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("Envío masivo completado: {} enviados exitosamente, {} con errores de {} usuarios totales",
                emailsEnviados, emailsConErrores, usuarios.size());
        return emailsEnviados;
    }

    private String generarContenidoAlerta(Usuario usuario, Alerta alerta) {
        Context context = new Context();

        context.setVariable("nombreUsuario", usuario.getNombre());
        context.setVariable("tipoAlerta", alerta.getTipo().getNombre());
        context.setVariable("descripcionAlerta", alerta.getTipo().getDescripcion());
        context.setVariable("mensaje", alerta.getMensaje());
        context.setVariable("distancia", alerta.getDistanciaDetectada());
        context.setVariable("ubicacion", alerta.getUbicacion() != null ? alerta.getUbicacion() : "Ubicación no especificada");
        context.setVariable("fechaHora", alerta.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        context.setVariable("dispositivoId", alerta.getDispositivoId() != null ? alerta.getDispositivoId() : "No especificado");
        context.setVariable("appName", appName);

        String template = determinarTemplate(alerta.getTipo());

        return templateEngine.process(template, context);
    }

    private String determinarTemplate(TipoAlerta tipoAlerta) {
        return switch (tipoAlerta) {
            case ALERTA_ROJA -> "emails/alerta-roja";
            case ALERTA_AMARILLA -> "emails/alerta-amarilla";
            case SITUACION_NORMALIZADA -> "emails/situacion-normalizada";
        };
    }

    private String generarSubject(TipoAlerta tipoAlerta) {
        return switch (tipoAlerta) {
            case ALERTA_ROJA -> "🚨 ALERTA ROJA - Riesgo Crítico de Inundación";
            case ALERTA_AMARILLA -> "⚠️ ALERTA AMARILLA - Precaución por Nivel de Agua Elevado";
            case SITUACION_NORMALIZADA -> "✅ Situación Normalizada - Nivel de Agua Estable";
        };
    }

    private void enviarEmail(String toEmail, String subject, String htmlContent) throws MessagingException {
        try {
            log.debug("Configurando mensaje de email...");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            log.debug("Enviando mensaje a través de JavaMailSender...");
            mailSender.send(message);
            log.debug("Mensaje enviado exitosamente a: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Error específico de mensajería: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Causa raíz: {}", e.getCause().getMessage());
            }
            throw e;
        }
    }
}
