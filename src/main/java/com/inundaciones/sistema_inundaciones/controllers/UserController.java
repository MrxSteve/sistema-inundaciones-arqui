package com.inundaciones.sistema_inundaciones.controllers;

import com.inundaciones.sistema_inundaciones.models.dto.response.UsuarioResponse;
import com.inundaciones.sistema_inundaciones.services.auth.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Endpoints para gestión de usuarios")
public class UserController {
    private final IUserService userService;

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios con paginación")
    public ResponseEntity<Page<UsuarioResponse>> getAll(Pageable pageable) {
        Page<UsuarioResponse> users = userService.findAll(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/name")
    @Operation(summary = "Buscar usuarios por nombre con paginación")
    public ResponseEntity<Page<UsuarioResponse>> findByName(@RequestParam String name, Pageable pageable) {
        Page<UsuarioResponse> users = userService.buscarPorNombreContaining(name, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/email")
    @Operation(summary = "Buscar usuario por email")
    public ResponseEntity<UsuarioResponse> findByEmail(@RequestParam String email) {
        return userService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por ID")
    public ResponseEntity<UsuarioResponse> findById(@PathVariable Long id) {
        return userService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/activate/{id}")
    public ResponseEntity<UsuarioResponse> activateUser(@PathVariable Long id) {
        UsuarioResponse user = userService.activar(id);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<UsuarioResponse> deactivateUser(@PathVariable Long id) {
        UsuarioResponse user = userService.desactivar(id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
