-- =====================================================
-- SCRIPT PARA CREAR BASE DE DATOS ESP32 IOT
-- =====================================================

-- Crear base de datos
CREATE DATABASE IF NOT EXISTS esp32_iot;
USE esp32_iot;

-- Crear tabla principal de sensor_datos
CREATE TABLE IF NOT EXISTS sensor_datos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp BIGINT NOT NULL,
    temperatura DOUBLE NOT NULL,
    humedad DOUBLE NOT NULL,
    luz_detectada BOOLEAN NOT NULL,
    teclado_input VARCHAR(255),
    ultima_tarjeta_rfid VARCHAR(20),
    esp32_ip VARCHAR(20),
    rssi INT,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Índices para optimización
    INDEX idx_fecha (fecha_creacion),
    INDEX idx_rfid (ultima_tarjeta_rfid),
    INDEX idx_ip (esp32_ip),
    INDEX idx_temperatura (temperatura),
    INDEX idx_humedad (humedad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Crear tabla para registro de accesos RFID
CREATE TABLE IF NOT EXISTS rfid_accesos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rfid_tag VARCHAR(20) NOT NULL UNIQUE,
    nombre_usuario VARCHAR(100),
    pin_acceso VARCHAR(10),
    ultimo_acceso DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    contador_accesos INT DEFAULT 1,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_rfid (rfid_tag),
    INDEX idx_ultimo_acceso (ultimo_acceso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Crear tabla para alertas
CREATE TABLE IF NOT EXISTS alertas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_alerta VARCHAR(50) NOT NULL,
    descripcion TEXT,
    valor_leido DOUBLE,
    valor_limite DOUBLE,
    fecha_alerta DATETIME DEFAULT CURRENT_TIMESTAMP,
    resuelta BOOLEAN DEFAULT FALSE,
    
    INDEX idx_tipo (tipo_alerta),
    INDEX idx_fecha (fecha_alerta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar datos de ejemplo
INSERT INTO sensor_datos (timestamp, temperatura, humedad, luz_detectada, teclado_input, ultima_tarjeta_rfid, esp32_ip, rssi)
VALUES
(1000, 23.5, 65.0, TRUE, '1234', 'AB12CD34', '192.168.1.250', -45),
(2000, 24.0, 64.5, TRUE, '5678', 'EF56GH78', '192.168.1.250', -42),
(3000, 23.8, 65.2, FALSE, '9012', 'IJ90KL12', '192.168.1.250', -48);

-- Insertar RFID usuarios
INSERT INTO rfid_accesos (rfid_tag, nombre_usuario, contador_accesos)
VALUES
('AB12CD34', 'Usuario 1', 5),
('EF56GH78', 'Usuario 2', 3),
('IJ90KL12', 'Usuario 3', 1);

-- Ver estructura de tablas
SHOW TABLES;
DESC sensor_datos;
DESC rfid_accesos;
DESC alertas;

-- Ver primeros registros
SELECT * FROM sensor_datos LIMIT 5;
SELECT * FROM rfid_accesos;
