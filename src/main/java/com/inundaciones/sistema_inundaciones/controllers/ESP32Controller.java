package com.inundaciones.sistema_inundaciones.controllers;

import com.inundaciones.sistema_inundaciones.services.esp32.ESP32Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/esp32")
@RequiredArgsConstructor
@Slf4j
public class ESP32Controller {

    private final ESP32Service esp32Service;

    @GetMapping("/numeros-sms")
    public ResponseEntity<Map<String, Object>> obtenerNumerosSMS() {
        Map<String, Object> response = esp32Service.obtenerNumerosSMS();

        boolean success = (boolean) response.get("success");

        return success
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

