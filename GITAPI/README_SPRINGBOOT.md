# ESP32 IoT Server - SpringBoot API

API REST en SpringBoot para recibir, almacenar y gestionar datos del ESP32 con CRUD completo.

## 📋 Características

✅ **CRUD Completo** - Crear, Leer, Actualizar, Eliminar  
✅ **Base de datos MySQL** con Hibernate/JPA  
✅ **Recibe JSON** del ESP32 cada 10 segundos  
✅ **Búsquedas avanzadas** - por fecha, RFID, temperatura, etc.  
✅ **Estadísticas** - máximas, promedios, conteos  
✅ **API RESTful** con respuestas en JSON  

## 🚀 Instalación y Configuración

### 1. Requisitos previos
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### 2. Crear Base de Datos

```sql
CREATE DATABASE esp32_iot;
USE esp32_iot;

CREATE TABLE sensor_datos (
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
    INDEX idx_fecha (fecha_creacion),
    INDEX idx_rfid (ultima_tarjeta_rfid),
    INDEX idx_ip (esp32_ip)
);
```

### 3. Configurar application.properties

Edita `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/esp32_iot
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD
```

### 4. Compilar y Ejecutar

```bash
# Compilar
mvn clean package

# Ejecutar
mvn spring-boot:run

# O directamente
java -jar target/esp32-iot-server-1.0.0.jar
```

El servidor estará disponible en: **http://localhost:8080**

## 📡 Endpoints de la API

### Health Check
```
GET /api/sensor-data/health
```

### CREATE - Guardar datos
```
POST /api/sensor-data
Content-Type: application/json

{
  "timestamp": 45000,
  "temperatura": 24.5,
  "humedad": 65.3,
  "luzDetectada": true,
  "tecladoInput": "1234",
  "ultimaTarjetaRfid": "AB12CD34",
  "esp32Ip": "192.168.1.250",
  "rssi": -45
}

Response: 201 Created
{
  "success": true,
  "message": "Datos guardados correctamente",
  "id": 1,
  "timestamp": 45000
}
```

### READ - Obtener todos los datos
```
GET /api/sensor-data
```

### READ - Obtener por ID
```
GET /api/sensor-data/1
```

### READ - Últimas N lecturas
```
GET /api/sensor-data/latest/10
```

### READ - Buscar por RFID
```
GET /api/sensor-data/rfid/AB12CD34
```

### READ - Por rango de fechas
```
GET /api/sensor-data/date-range?startDate=2024-01-30T10:00:00&endDate=2024-01-30T12:00:00
```

### READ - Datos con luz detectada
```
GET /api/sensor-data/light-detected
```

### UPDATE - Actualizar registro
```
PUT /api/sensor-data/1
Content-Type: application/json

{
  "temperatura": 25.0,
  "humedad": 70.0
}
```

### DELETE - Eliminar registro
```
DELETE /api/sensor-data/1
```

### ESTADÍSTICAS - Total de registros
```
GET /api/sensor-data/stats/total-records
```

### ESTADÍSTICAS - Temperatura máxima en rango
```
GET /api/sensor-data/stats/temperature-max?startDate=2024-01-30T00:00:00&endDate=2024-01-31T00:00:00
```

### ESTADÍSTICAS - Promedio de humedad
```
GET /api/sensor-data/stats/humidity-avg?startDate=2024-01-30T00:00:00&endDate=2024-01-31T00:00:00
```

## 📊 Estructura de Base de Datos

```sql
sensor_datos
├── id (PK, AUTO_INCREMENT)
├── timestamp (BIGINT)
├── temperatura (DOUBLE)
├── humedad (DOUBLE)
├── luz_detectada (BOOLEAN)
├── teclado_input (VARCHAR 255)
├── ultima_tarjeta_rfid (VARCHAR 20)
├── esp32_ip (VARCHAR 20)
├── rssi (INT)
└── fecha_creacion (DATETIME, AUTO)
```

## 🔌 Integración con ESP32

El código Arduino enviará JSON cada 10 segundos a:

```
POST http://192.168.1.249:8080/api/sensor-data
```

Ejemplo del JSON que envía el ESP32:
```json
{
  "timestamp": 45000,
  "temperatura": 24.5,
  "humedad": 65.3,
  "luz_detectada": true,
  "teclado_input": "1234",
  "ultima_tarjeta_rfid": "AB12CD34",
  "esp32_ip": "192.168.1.250",
  "rssi": -45
}
```

## 🛠️ Herramientas para Probar

### Usar cURL
```bash
# Crear registro
curl -X POST http://localhost:8080/api/sensor-data \
  -H "Content-Type: application/json" \
  -d '{"timestamp": 45000, "temperatura": 24.5, "humedad": 65.3, "luzDetectada": true}'

# Obtener todos
curl http://localhost:8080/api/sensor-data

# Obtener por ID
curl http://localhost:8080/api/sensor-data/1
```

### Usar Postman
1. Crear nueva colección
2. Importar endpoints de la API
3. Enviar requests

## 📝 Notas

- ✅ Los pines del ESP32 están configurados correctamente
- ✅ La API recibe datos cada 10 segundos
- ✅ Todos los datos se guardan en MySQL
- ✅ Incluye validación y manejo de errores
- ✅ CORS habilitado para acceder desde otras IPs

## 🔐 Seguridad

Para producción, considera:
- Agregar autenticación JWT
- Validación de entrada más estricta
- HTTPS en lugar de HTTP
- Rate limiting
- Logs más detallados

## 📞 Soporte

Si tienes problemas:
1. Verifica que MySQL esté corriendo
2. Verifica la base de datos existe
3. Revisa los logs de la aplicación
4. Comprueba la conexión de red del ESP32

---

**Versión:** 1.0.0  
**Estado:** Listo para Producción
