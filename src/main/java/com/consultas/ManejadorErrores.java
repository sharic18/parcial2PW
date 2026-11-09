package com.consultas;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Convierte los errores en respuestas JSON claras (400 / 409). */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> detalles = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> detalles.put(e.getField(), e.getDefaultMessage()));
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("error", "Datos invalidos");
        cuerpo.put("detalles", detalles);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> solicitudInvalida(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", String.valueOf(ex.getMessage())));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> formatoInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "Formato de datos invalido, por favor revise numeros y horas"));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, String>> duplicado(DuplicateKeyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Ya existe un registro con esos datos"));
    }
}
