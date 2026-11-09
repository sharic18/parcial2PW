
-- Contrasenas guardadas como SHA-256 

DROP TABLE IF EXISTS consultas;
DROP TABLE IF EXISTS medicos;
DROP TABLE IF EXISTS usuarios;

CREATE TABLE usuarios (
    id        SERIAL PRIMARY KEY,
    username  VARCHAR(30)  NOT NULL UNIQUE,
    password  VARCHAR(100) NOT NULL,
    rol       VARCHAR(10)  NOT NULL CHECK (rol IN ('ADMIN','MEDICO','PACIENTE')),
    nombre    VARCHAR(40)  NOT NULL
);

CREATE TABLE medicos (
    id            SERIAL PRIMARY KEY,
    nombre        VARCHAR(40) NOT NULL,
    especialidad  VARCHAR(60) NOT NULL,
    usuario_id    INT UNIQUE REFERENCES usuarios(id)
);

CREATE TABLE consultas (
    id                  SERIAL PRIMARY KEY,
    nombre_paciente     VARCHAR(40)  NOT NULL,
    motivo              VARCHAR(100) NOT NULL,
    numero_consultorio  INT          NOT NULL,
    hora_inicio         TIME         NOT NULL,
    hora_fin            TIME         NOT NULL,
    medico_id           INT          NOT NULL REFERENCES medicos(id),
    CHECK (hora_fin > hora_inicio)
);

INSERT INTO usuarios (username, password, rol, nombre) VALUES
('admin',   encode(sha256(convert_to('admin123',    'UTF8')), 'hex'), 'ADMIN',    'Administrador'),
('drperez', encode(sha256(convert_to('medico123',   'UTF8')), 'hex'), 'MEDICO',   'Carlos Perez'),
('drlopez', encode(sha256(convert_to('medico123',   'UTF8')), 'hex'), 'MEDICO',   'Laura Lopez'),
('ana',     encode(sha256(convert_to('paciente123', 'UTF8')), 'hex'), 'PACIENTE', 'Ana Torres'),
('luis',    encode(sha256(convert_to('paciente123', 'UTF8')), 'hex'), 'PACIENTE', 'Luis Gomez');

INSERT INTO medicos (nombre, especialidad, usuario_id) VALUES
('Carlos Perez', 'Medicina General', (SELECT id FROM usuarios WHERE username = 'drperez')),
('Laura Lopez',  'Pediatria',        (SELECT id FROM usuarios WHERE username = 'drlopez'));

INSERT INTO consultas (nombre_paciente, motivo, numero_consultorio, hora_inicio, hora_fin, medico_id) VALUES
('Ana Torres', 'Control general',  101, '08:00', '08:30', (SELECT id FROM medicos WHERE nombre = 'Carlos Perez')),
('Luis Gomez', 'Dolor de cabeza',  102, '09:00', '09:30', (SELECT id FROM medicos WHERE nombre = 'Laura Lopez'));
