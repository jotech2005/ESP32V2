# Sistema de Autenticación RFID con PIN - Implementación Completada

## 📋 Descripción General
Se ha implementado un sistema completo de autenticación por tarjeta RFID con PIN. Cuando se pasa una tarjeta:
- **Si es NUEVA**: Se solicita un PIN (mínimo 4 dígitos) que se registra en la base de datos
- **Si es REGISTRADA**: Se solicita el PIN y se verifica
  - ✓ PIN correcto → "ADELANTE"
  - ✗ PIN incorrecto → "ACCESO DENEGADO"

---

## 🔧 Cambios Realizados

### 1. BASE DE DATOS (database_setup.sql)
**Tabla modificada: `rfid_accesos`**
- ✅ Agregado campo `pin_acceso` (VARCHAR(10))
- ✅ Agregado campo `fecha_creacion` (DATETIME)
- Estructura para almacenar y validar PINs

### 2. ARDUINO (ESP32_IoT_System.ino)

#### Variables Globales Nuevas:
```cpp
String cardPinInput = "";           // Entrada de PIN
String currentCardUID = "";         // UID actual siendo procesado
bool waitingForPIN = false;         // Flag de espera de PIN
unsigned long pinEntryTimeout = 0;  // Timeout de entrada
const unsigned long PIN_TIMEOUT = 30000;  // 30 segundos
const int MAX_PIN_LENGTH = 6;       // Máximo 6 dígitos
```

#### Funciones Nuevas:

1. **`readKeypadForPIN()`** 
   - Lee el teclado cuando se espera PIN
   - `#` = confirmar PIN
   - `*` = cancelar
   - Dígitos (0-9) = agregar al PIN

2. **`displayPINEntry()`**
   - Muestra pantalla de entrada de PIN
   - Muestra asteriscos (*) en lugar del PIN real
   - Instrucciones en LCD

3. **`verifyCardPIN()`**
   - Envía solicitud POST a `/api/rfid-auth`
   - Recibe respuesta del servidor
   - Muestra "✓ ADELANTE" o "✗ ACCESO DENEGADO"

#### Funciones Modificadas:

1. **`setup()`**
   - Ahora inicializa el teclado

2. **`loop()`**
   - Verifica si está esperando PIN
   - Si no, lee RFID normalmente

3. **`readRFID()`**
   - Al detectar tarjeta, activa el modo de entrada de PIN
   - No envía datos inmediatamente
   - Espera validación de PIN

### 3. BACKEND SPRING BOOT

#### DTOs (Nuevos):

**`RFIDAuthRequestDTO.java`**
```json
{
  "rfid_tag": "1A2B3C4D",
  "pin_ingresado": "1234",
  "accion": "verificar_pin"
}
```

**`RFIDAuthResponseDTO.java`**
```json
{
  "autenticado": true,
  "es_nueva": true,
  "mensaje": "Tarjeta registrada con PIN exitosamente",
  "rfid_tag": "1A2B3C4D"
}
```

#### Modelos (Nuevos):

**`RFIDAcceso.java`**
- Entity JPA para tabla `rfid_accesos`
- Campos:
  - `rfidTag` - UID de la tarjeta (UNIQUE)
  - `pinAcceso` - PIN de acceso (máx 10 caracteres)
  - `nombreUsuario` - Opcional
  - `activo` - Tarjeta activa/desactivada
  - `contadorAccesos` - Contador de usos
  - `ultimoAcceso` - Timestamp último acceso
  - `fechaCreacion` - Timestamp creación

#### Repositorios (Nuevos):

**`RFIDAccesoRepository.java`**
- `findByRfidTag(String)` - Buscar tarjeta
- `existsByRfidTag(String)` - Verificar existencia

#### Servicios (Nuevos):

**`RFIDAuthService.java`**
- `verificarORegistrarPIN()` - Lógica principal
  - Si tarjeta no existe → Crear con PIN
  - Si existe → Verificar PIN
  - Actualizar contador y último acceso
- `cambiarPIN()` - Cambiar PIN
- `desactivarTarjeta()` - Desactivar acceso
- `obtenerAccesoPorRFID()` - Consultar tarjeta

#### Controladores (Nuevos):

**`RFIDAuthController.java`**
- `POST /api/rfid-auth` - Verificar/registrar PIN
- `PUT /api/rfid-auth/cambiar-pin` - Cambiar PIN
- `DELETE /api/rfid-auth/{rfidTag}` - Desactivar tarjeta
- `GET /api/rfid-auth/{rfidTag}` - Consultar tarjeta

---

## 🔄 Flujo de Operación

### Caso 1: Tarjeta NUEVA
```
1. Usuario pasa tarjeta RFID
2. ESP32 detecta tarjeta
3. LCD muestra: "TARJETA: 1A2B3C4D"
4. ESP32 solicita PIN:
   LCD: "PIN: ****"
   "# confirmar"
5. Usuario ingresa 4+ dígitos
6. Usuario presiona #
7. ESP32 envía: POST /api/rfid-auth
   {"rfid_tag": "1A2B3C4D", "pin_ingresado": "1234"}
8. Servidor responde: {"autenticado": true, "es_nueva": true}
9. LCD muestra: "✓ ADELANTE"
   "PIN registrado"
10. Tarjeta registrada en BD con PIN
```

### Caso 2: Tarjeta REGISTRADA (PIN Correcto)
```
1. Usuario pasa tarjeta RFID
2. ESP32 detecta tarjeta
3. LCD muestra: "TARJETA: 1A2B3C4D"
4. Solicita PIN
5. Usuario ingresa PIN correcto
6. Presiona #
7. Servidor verifica PIN
8. Respuesta: {"autenticado": true, "es_nueva": false}
9. LCD muestra: "✓ ADELANTE"
   "Bienvenido!"
```

### Caso 3: Tarjeta REGISTRADA (PIN Incorrecto)
```
1-5. (Igual a Caso 2)
6. Usuario ingresa PIN incorrecto
7. Presiona #
8. Servidor verifica PIN → NO COINCIDE
9. Respuesta: {"autenticado": false}
10. LCD muestra: "✗ ACCESO DENEGADO"
    "PIN incorrecto"
```

### Caso 4: Timeout (30 segundos sin ingresar PIN)
```
- Si pasa 30 segundos sin confirmar PIN
- LCD: "TIMEOUT"
  "Acceso cancelado"
- Sistema vuelve al estado normal
```

---

## 🛠️ Configuración Requerida

### Database
```sql
-- Ejecutar:
ALTER TABLE rfid_accesos 
ADD COLUMN pin_acceso VARCHAR(10),
ADD COLUMN fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP;
```

### Arduino
- Teclado 4x4 debe estar conectado en pines correctos
- WiFi debe estar conectado
- Teclado se lee cuando `waitingForPIN = true`

### Spring Boot
- Agregar dependencia ArduinoJson en Arduino IDE
- Spring Data JPA maneja creación de tablas automáticamente
- Compilar y ejecutar `./mvnw spring-boot:run`

---

## 📱 Botones del Teclado

| Botón | Acción | Contexto |
|-------|--------|----------|
| 0-9   | Ingresar dígito | Siempre en modo PIN |
| `#`   | Confirmar PIN | Mínimo 4 dígitos |
| `*`   | Cancelar | En cualquier momento |
| A-D   | Reservados | Usos futuros |

---

## 🔒 Seguridad

- PIN almacenado en texto plano en BD (considerar encripción con BCrypt)
- Timeout de 30 segundos para seguridad
- Flag `activo` para desactivar tarjetas sin eliminarlas
- Contador de accesos para auditoría
- Timestamp de último acceso

---

## ✅ Pruebas Sugeridas

1. **Tarjeta nueva**: Pasar tarjeta desconocida, ingresar PIN, confirmar registro
2. **Acceso correcto**: Pasar tarjeta registrada, ingresar PIN correcto
3. **Acceso denegado**: Pasar tarjeta registrada, ingresar PIN incorrecto
4. **Cancelar**: Presionar `*` durante entrada de PIN
5. **Timeout**: Esperar 30+ segundos sin confirmar
6. **PIN corto**: Intentar confirmar con menos de 4 dígitos

---

## 📝 Notas

- El PIN es visible como asteriscos en LCD para privacidad
- Mínimo 4 dígitos requeridos (puede ajustarse en código)
- El sistema no acepta PINs vacíos
- Se puede mejorar con encriptación BCrypt
- Es posible agregar logs de acceso más detallados
