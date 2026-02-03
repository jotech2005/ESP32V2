/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE DATABASE IF NOT EXISTS `esp32_iot` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;
USE `esp32_iot`;

CREATE TABLE IF NOT EXISTS `alertas` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tipo_alerta` varchar(50) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `valor_leido` double DEFAULT NULL,
  `valor_limite` double DEFAULT NULL,
  `fecha_alerta` datetime DEFAULT current_timestamp(),
  `resuelta` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tipo` (`tipo_alerta`),
  KEY `idx_fecha` (`fecha_alerta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS `rfid_accesos` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `rfid_tag` varchar(20) NOT NULL,
  `nombre_usuario` varchar(100) DEFAULT NULL,
  `ultimo_acceso` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `contador_accesos` int(11) DEFAULT 1,
  `activo` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `rfid_tag` (`rfid_tag`),
  KEY `idx_rfid` (`rfid_tag`),
  KEY `idx_ultimo_acceso` (`ultimo_acceso`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `rfid_accesos` (`id`, `rfid_tag`, `nombre_usuario`, `ultimo_acceso`, `contador_accesos`, `activo`) VALUES
	(1, 'AB12CD34', 'Usuario 1', '2026-01-30 11:54:04', 5, 1),
	(2, 'EF56GH78', 'Usuario 2', '2026-01-30 11:54:04', 3, 1),
	(3, 'IJ90KL12', 'Usuario 3', '2026-01-30 11:54:04', 1, 1);

CREATE TABLE IF NOT EXISTS `sensor_datos` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `timestamp` bigint(20) NOT NULL,
  `temperatura` double NOT NULL,
  `humedad` double NOT NULL,
  `luz_detectada` tinyint(1) NOT NULL,
  `teclado_input` varchar(255) DEFAULT NULL,
  `ultima_tarjeta_rfid` varchar(20) DEFAULT NULL,
  `esp32_ip` varchar(20) DEFAULT NULL,
  `rssi` int(11) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_fecha` (`fecha_creacion`),
  KEY `idx_rfid` (`ultima_tarjeta_rfid`),
  KEY `idx_ip` (`esp32_ip`),
  KEY `idx_temperatura` (`temperatura`),
  KEY `idx_humedad` (`humedad`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `sensor_datos` (`id`, `timestamp`, `temperatura`, `humedad`, `luz_detectada`, `teclado_input`, `ultima_tarjeta_rfid`, `esp32_ip`, `rssi`, `fecha_creacion`) VALUES
	(1, 1000, 23.5, 65, 1, '1234', 'AB12CD34', '192.168.1.250', -45, '2026-01-30 11:54:04'),
	(2, 2000, 24, 64.5, 1, '5678', 'EF56GH78', '192.168.1.250', -42, '2026-01-30 11:54:04'),
	(3, 3000, 23.8, 65.2, 0, '9012', 'IJ90KL12', '192.168.1.250', -48, '2026-01-30 11:54:04'),
	(4, 45109, 22.6, 55.3, 0, '123A456B789C*0#D', '', '192.168.1.109', -21, '2026-01-30 12:27:56'),
	(5, 58986, 22.6, 55.2, 0, '123A456B789C*0#D', '', '192.168.1.109', -23, '2026-01-30 12:28:09'),
	(6, 72040, 22.8, 55.2, 0, '123A456B789C*0#D', '', '192.168.1.109', -23, '2026-01-30 12:28:22'),
	(7, 84883, 22.9, 55, 0, '123A456B789C*0#D', '', '192.168.1.109', -23, '2026-01-30 12:28:35'),
	(8, 97636, 23, 55, 0, '123A456B789C*0#D', '', '192.168.1.109', -22, '2026-01-30 12:28:48'),
	(9, 110329, 23, 54.8, 0, '123A456B789C*0#D', '', '192.168.1.109', -54, '2026-01-30 12:29:00'),
	(10, 123062, 23, 55.4, 0, '123A456B789C*0#D', '', '192.168.1.109', -55, '2026-01-30 12:29:13'),
	(11, 135836, 22.9, 55.7, 0, '123A456B789C*0#D', '', '192.168.1.109', -56, '2026-01-30 12:29:26'),
	(12, 10070, 23.5, 55, 1, '', '', '192.168.1.109', -33, '2026-01-30 12:39:35'),
	(13, 20509, 23.5, 55, 1, '', '', '192.168.1.109', -33, '2026-01-30 12:39:45'),
	(14, 30732, 23.5, 55, 1, '', '', '192.168.1.109', -37, '2026-01-30 12:39:55'),
	(15, 40985, 23.5, 54.9, 1, '', '', '192.168.1.109', -36, '2026-01-30 12:40:05'),
	(16, 55177, 23.4, 56.7, 0, '', '', '192.168.1.250', -57, '2026-01-30 12:47:43'),
	(17, 10024, 23.5, 56.4, 0, '', '', '192.168.1.250', -54, '2026-01-30 12:48:14'),
	(18, 20726, 23.6, 56.4, 0, '', '', '192.168.1.250', -59, '2026-01-30 12:48:24'),
	(19, 30941, 23.6, 56.4, 0, '', '', '192.168.1.250', -57, '2026-01-30 12:48:34'),
	(20, 41254, 23.6, 56.4, 0, '', '', '192.168.1.250', -58, '2026-01-30 12:48:45'),
	(21, 51577, 23.6, 56.3, 0, '', '', '192.168.1.250', -55, '2026-01-30 12:48:55');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
