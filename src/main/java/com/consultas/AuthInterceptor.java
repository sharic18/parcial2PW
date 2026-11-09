package com.consultas;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.consultas.Dtos.UsuarioSesion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Filtro de autenticacion y autorizacion.
 * 401 = no hay sesion iniciada. 403 = hay sesion pero el rol no corresponde.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String ruta = req.getRequestURI();
        String rolRequerido = null;
        if (ruta.startsWith("/api/admin")) rolRequerido = "ADMIN";
        else if (ruta.startsWith("/api/medico")) rolRequerido = "MEDICO";
        else if (ruta.startsWith("/api/paciente")) rolRequerido = "PACIENTE";

        if (rolRequerido == null) return true; 

        HttpSession session = req.getSession(false);
        UsuarioSesion u = (session == null) ? null : (UsuarioSesion) session.getAttribute("usuario");

        res.setContentType("application/json;charset=UTF-8");
        if (u == null) {
            res.setStatus(401);
            res.getWriter().write("{\"error\":\"No autenticado. Inicie sesion.\"}");
            return false;
        }
        if (!u.rol().equals(rolRequerido)) {
            res.setStatus(403);
            res.getWriter().write("{\"error\":\"Su rol no tiene permisos para esta accion.\"}");
            return false;
        }
        return true;
    }
}
