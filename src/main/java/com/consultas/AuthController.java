package com.consultas;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.consultas.Dtos.LoginRequest;
import com.consultas.Dtos.UsuarioSesion;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Inicio y cierre de sesion")
public class AuthController {

    private final JdbcTemplate jdbc;

    public AuthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Operation(summary = "Iniciar sesion", description = "Valida usuario y clave, crea la sesion y devuelve nombre y rol.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto"),
            @ApiResponse(responseCode = "401", description = "Usuario o clave incorrectos", content = @Content),
            @ApiResponse(responseCode = "403", description = "No aplica: el login es publico", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        if (req.username() == null || req.password() == null
                || req.username().isBlank() || req.password().isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Ingrese usuario y clave"));
        }
        List<UsuarioSesion> encontrados = jdbc.query(
                "SELECT id, username, rol, nombre FROM usuarios WHERE username = ? AND password = ?",
                (rs, i) -> new UsuarioSesion(rs.getInt("id"), rs.getString("username"),
                        rs.getString("rol"), rs.getString("nombre")),
                req.username().trim(), Util.sha256(req.password()));

        if (encontrados.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Usuario o clave incorrectos"));
        }
        HttpSession anterior = request.getSession(false);
        if (anterior != null) anterior.invalidate();        
        UsuarioSesion u = encontrados.get(0);
        request.getSession(true).setAttribute("usuario", u);
        return ResponseEntity.ok(Map.of("nombre", u.nombre(), "rol", u.rol()));
    }

    @Operation(summary = "Cerrar sesion", description = "Invalida la sesion actual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesion cerrada"),
            @ApiResponse(responseCode = "401", description = "No aplica: cerrar sesion no exige estar autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "No aplica: sin restriccion por rol", content = @Content)
    })
    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return Map.of("mensaje", "Sesion cerrada");
    }

    @Operation(summary = "Usuario actual", description = "Devuelve nombre y rol de la sesión activa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hay una sesion activa"),
            @ApiResponse(responseCode = "401", description = "No hay sesion iniciada", content = @Content),
            @ApiResponse(responseCode = "403", description = "No aplica: sin restriccion por rol", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        UsuarioSesion u = (session == null) ? null : (UsuarioSesion) session.getAttribute("usuario");
        if (u == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        }
        return ResponseEntity.ok(Map.of("nombre", u.nombre(), "rol", u.rol()));
    }
}
