package com.consultas;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.consultas.Dtos.UsuarioSesion;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/medico")
@Tag(name = "Medico", description = "Consulta de citas asignadas solo para lectura")
public class MedicoController {

    private final JdbcTemplate jdbc;

    public MedicoController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Operation(summary = "Ver mis consultas", description = "Devuelve unicamente las consultas asignadas al medico de la sesion.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consultas del medico"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol sin permisos", content = @Content)
    })
    @GetMapping("/consultas")
    public List<Map<String, Object>> misConsultas(HttpSession session) {
        UsuarioSesion u = (UsuarioSesion) session.getAttribute("usuario");
        return jdbc.queryForList("""
                SELECT c.id, c.nombre_paciente, c.motivo, c.numero_consultorio,
                       c.hora_inicio, c.hora_fin, m.nombre AS medico
                FROM consultas c JOIN medicos m ON m.id = c.medico_id
                WHERE m.usuario_id = ?
                ORDER BY c.hora_inicio""", u.id());
    }
}
