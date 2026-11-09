package com.consultas;

import java.sql.Time;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.consultas.Dtos.ConsultaRequest;
import com.consultas.Dtos.MedicoRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administrador", description = "Gestion de medicos y consultas solo con permisos de administrador")
public class AdminController {

    private final JdbcTemplate jdbc;

    public AdminController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

// MEDICOS
    
    @Operation(summary = "Listar medicos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de medicos"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol sin permisos", content = @Content)
    })
    @GetMapping("/medicos")
    public List<Map<String, Object>> listarMedicos() {
        return jdbc.queryForList("""
                SELECT m.id, m.nombre, m.especialidad, u.username
                FROM medicos m LEFT JOIN usuarios u ON u.id = m.usuario_id
                ORDER BY m.nombre""");
    }

    @Operation(summary = "Crear medico", description = "Crea el medico y su usuario de acceso.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medico creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol sin permisos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Usuario repetido", content = @Content)
    })
    @PostMapping("/medicos")
    @Transactional
    public Map<String, Object> crearMedico(@Valid @RequestBody MedicoRequest r) {
        if (r.username() == null || r.username().isBlank() || r.password() == null || r.password().isBlank()) {
            throw new IllegalArgumentException("Usuario y clave son obligatorios al crear un medico");
        }
        Integer usuarioId = jdbc.queryForObject(
                "INSERT INTO usuarios (username, password, rol, nombre) VALUES (?, ?, 'MEDICO', ?) RETURNING id",
                Integer.class, r.username().trim(), Util.sha256(r.password()), r.nombre().trim());
        Integer medicoId = jdbc.queryForObject(
                "INSERT INTO medicos (nombre, especialidad, usuario_id) VALUES (?, ?, ?) RETURNING id",
                Integer.class, r.nombre().trim(), r.especialidad().trim(), usuarioId);
        return Map.of("id", medicoId, "mensaje", "Medico creado");
    }

    @Operation(summary = "Editar medico", description = "Actualiza nombre y especialidad.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medico actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol sin permisos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Medico no existe", content = @Content)
    })
    @PutMapping("/medicos/{id}")
    @Transactional
    public Map<String, String> editarMedico(@PathVariable int id, @Valid @RequestBody MedicoRequest r) {
        int filas = jdbc.update("UPDATE medicos SET nombre = ?, especialidad = ? WHERE id = ?",
                r.nombre().trim(), r.especialidad().trim(), id);
        if (filas == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El medico no existe");
        jdbc.update("UPDATE usuarios SET nombre = ? WHERE id = (SELECT usuario_id FROM medicos WHERE id = ?)",
                r.nombre().trim(), id);
        return Map.of("mensaje", "Medico actualizado");
    }

    @Operation(summary = "Eliminar medico", description = "Elimina el medico, sus consultas y su usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medico eliminado"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol sin permisos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Medico no existe", content = @Content)
    })
    @DeleteMapping("/medicos/{id}")
    @Transactional
    public Map<String, String> eliminarMedico(@PathVariable int id) {
        List<Integer> usuarios = jdbc.queryForList("SELECT usuario_id FROM medicos WHERE id = ?", Integer.class, id);
        if (usuarios.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El medico no existe");
        jdbc.update("DELETE FROM consultas WHERE medico_id = ?", id);
        jdbc.update("DELETE FROM medicos WHERE id = ?", id);
        if (usuarios.get(0) != null) jdbc.update("DELETE FROM usuarios WHERE id = ?", usuarios.get(0));
        return Map.of("mensaje", "Medico eliminado");
    }

// CONSULTAS 
    
    @Operation(summary = "Listar todas las consultas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de consultas"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol sin permisos", content = @Content)
    })
    @GetMapping("/consultas")
    public List<Map<String, Object>> listarConsultas() {
        return jdbc.queryForList("""
                SELECT c.id, c.nombre_paciente, c.motivo, c.numero_consultorio,
                       c.hora_inicio, c.hora_fin, m.nombre AS medico
                FROM consultas c JOIN medicos m ON m.id = c.medico_id
                ORDER BY c.hora_inicio""");
    }

    @Operation(summary = "Asignar consultorio y crear consulta",
            description = "Paciente alfanumerico max 40, motivo max 100, consultorio numerico, horario inicio/fin y medico existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Rol sin permisos", content = @Content)
    })
    @PostMapping("/consultas")
    public Map<String, String> crearConsulta(@Valid @RequestBody ConsultaRequest r) {
        Util.validarHorario(r.horaInicio(), r.horaFin());
        Integer existe = jdbc.queryForObject("SELECT COUNT(*) FROM medicos WHERE id = ?", Integer.class, r.medicoId());
        if (existe == null || existe == 0) throw new IllegalArgumentException("El medico seleccionado no existe");
        jdbc.update("""
                INSERT INTO consultas (nombre_paciente, motivo, numero_consultorio, hora_inicio, hora_fin, medico_id)
                VALUES (?, ?, ?, ?, ?, ?)""",
                r.nombrePaciente().trim(), r.motivo().trim(), r.numeroConsultorio(),
                Time.valueOf(r.horaInicio()), Time.valueOf(r.horaFin()), r.medicoId());
        return Map.of("mensaje", "Consulta registrada");
    }
}
