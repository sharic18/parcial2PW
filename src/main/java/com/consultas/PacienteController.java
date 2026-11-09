package com.consultas;

import java.sql.Time;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.consultas.Dtos.HorarioRequest;
import com.consultas.Dtos.UsuarioSesion;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/paciente")
@Tag(name = "Paciente", description = "Ver y reprogramar SUS citas (solo rol PACIENTE)")
public class PacienteController {

    private final JdbcTemplate jdbc;

    public PacienteController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Operation(summary = "Ver mis citas", description = "Devuelve solo las citas cuyo nombre de paciente coincide con el usuario de la sesion.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Citas del paciente"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol sin permisos", content = @Content)
    })
    @GetMapping("/consultas")
    public List<Map<String, Object>> misCitas(HttpSession session) {
        UsuarioSesion u = (UsuarioSesion) session.getAttribute("usuario");
        return jdbc.queryForList("""
                SELECT c.id, c.nombre_paciente, c.motivo, c.numero_consultorio,
                       c.hora_inicio, c.hora_fin, m.nombre AS medico
                FROM consultas c JOIN medicos m ON m.id = c.medico_id
                WHERE lower(c.nombre_paciente) = lower(?)
                ORDER BY c.hora_inicio""", u.nombre());
    }

    @Operation(summary = "Actualizar horario de una cita propia",
            description = "Solo permite modificar el horario de citas asignadas al paciente de la sesion.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Horario actualizado"),
            @ApiResponse(responseCode = "400", description = "Horario invalido", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol sin permisos o la cita no le pertenece", content = @Content)
    })
    @PutMapping("/consultas/{id}/horario")
    public Map<String, String> actualizarHorario(@PathVariable int id,
            @Valid @RequestBody HorarioRequest r, HttpSession session) {
        UsuarioSesion u = (UsuarioSesion) session.getAttribute("usuario");
        Util.validarHorario(r.horaInicio(), r.horaFin());
        int filas = jdbc.update("""
                UPDATE consultas SET hora_inicio = ?, hora_fin = ?
                WHERE id = ? AND lower(nombre_paciente) = lower(?)""",
                Time.valueOf(r.horaInicio()), Time.valueOf(r.horaFin()), id, u.nombre());
        if (filas == 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esa cita no le pertenece");
        }
        return Map.of("mensaje", "Horario actualizado");
    }
}
