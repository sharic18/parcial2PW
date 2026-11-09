# Gestion de Consultas Medicas

Aplicacion web para gestionar consultas medicas con roles ADMINISTRADOR, MEDICO y PACIENTE.

Spring Boot 3.5 (Tomcat 10.1 embebido) + PostgreSQL + Swagger (springdoc).

## Puesta en marcha
1. Crear la BD `consultas_medicas` en pgAdmin y ejecutar `sql/script_bd.sql`.
2. Ejecutar la clase `ConsultasApplication` 
3. App: http://localhost:8081/  |  Swagger: http://localhost:8081/swagger-ui.html

## Usuarios de prueba
| Rol | Usuario | Clave |
|---|---|---|
| ADMIN | admin | admin123 |
| MEDICO | drperez / drlopez | medico123 |
| PACIENTE | ana / luis | paciente123 |

## Permisos por rol
- ADMIN: crear, editar y eliminar medicos; crear consultas y asignar consultorios.
- MEDICO: solo ve sus consultas asignadas (solo lectura).
- PACIENTE: ve sus citas y actualiza solo el horario de las suyas.