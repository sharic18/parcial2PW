package com.consultas;

import java.io.Serializable;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Objetos que viajan entre el navegador y el servidor, con sus validaciones. */
public final class Dtos {
    private Dtos() {}

    /** Usuario guardado en la sesion HTTP despues del login. */
    public record UsuarioSesion(int id, String username, String rol, String nombre) implements Serializable {}

    public record LoginRequest(String username, String password) {}

    public record MedicoRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 40, message = "El nombre no debe superar 40 caracteres") String nombre,
            @NotBlank(message = "La especialidad es obligatoria")
            @Size(max = 60, message = "La especialidad no debe superar 60 caracteres") String especialidad,
            @Size(max = 30, message = "El usuario no debe superar 30 caracteres") String username,
            String password) {}

    public record ConsultaRequest(
            @NotBlank(message = "El nombre del paciente es obligatorio")
            @Size(max = 40, message = "El nombre del paciente no debe superar 40 caracteres")
            @Pattern(regexp = "^[\\p{L}\\p{N} ]*$", message = "El nombre del paciente solo admite letras y numeros, por favor ingrese un nombre valido")
            String nombrePaciente,
            @NotBlank(message = "El motivo es obligatorio")
            @Size(max = 100, message = "El motivo no debe superar 100 caracteres") String motivo,
            @NotNull(message = "El numero de consultorio es obligatorio")
            @Positive(message = "El consultorio debe ser un numero positivo") Integer numeroConsultorio,
            @NotNull(message = "La hora de inicio es obligatoria") LocalTime horaInicio,
            @NotNull(message = "La hora de fin es obligatoria") LocalTime horaFin,
            @NotNull(message = "Debe seleccionar un medico") Integer medicoId) {}

    public record HorarioRequest(
            @NotNull(message = "La hora de inicio es obligatoria") LocalTime horaInicio,
            @NotNull(message = "La hora de fin es obligatoria") LocalTime horaFin) {}
}
