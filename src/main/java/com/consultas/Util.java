package com.consultas;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalTime;
import java.util.HexFormat;

public final class Util {
    private Util() {}

    /** Convierte un texto a SHA-256 en hexadecimal. */
    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** La hora de fin debe ser posterior a la de inicio. */
    public static void validarHorario(LocalTime inicio, LocalTime fin) {
        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }
    }
}
