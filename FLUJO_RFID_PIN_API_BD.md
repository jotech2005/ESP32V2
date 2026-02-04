# 🔐 FLUJO COMPLETO: TARJETA RFID → PIN → API → BASE DE DATOS

## 📋 Descripción General

Este documento describe el flujo completo desde que se coloca una tarjeta RFID en el lector hasta que el PIN se verifica y guarda en la base de datos.

---

## 🔄 FLUJO PASO A PASO

### **PASO 1: Lectura de Tarjeta RFID (ESP32)**
**Archivo:** `ESP32_IoT_System.ino` - Función `readRFID()`

```
┌─────────────────────────────────────────┐
│ 1. Tarjeta presente en lector RC522     │
│ 2. ESP32 lee UID (hexadecimal)         │
│ 3. Ejemplo: "1A2B3C4D"                 │
│ 4. Activa modo espera de PIN           │
└─────────────────────────────────────────┘
```

**Detalles:**
- El módulo RFID RC522 detecta la tarjeta
- Se construye el UID en formato hexadecimal
- Se guarda en variable `currentCardUID`
- Se activa flag `waitingForPIN = true`
- Se muestra pantalla de entrada de PIN en LCD

---

### **PASO 2: Ingreso de PIN (ESP32 - Teclado 4x4)**
**Archivo:** `ESP32_IoT_System.ino` - Función `readKeypadForPIN()`

```
┌─────────────────────────────────────────┐
│ Usuario presiona teclas del teclado    │
│ Ejemplo: "1234" (mínimo 4 dígitos)    │
│ Presiona "#" para confirmar            │
│ Presiona "*" para cancelar              │
└─────────────────────────────────────────┘
```

**Validaciones:**
- Mínimo 4 dígitos
- Máximo 6 dígitos
- Timeout: 30 segundos
- Se muestran asteriscos (*) en LCD por seguridad

---

### **PASO 3: Envío por API HTTP POST (ESP32)**
**Archivo:** `ESP32_IoT_System.ino` - Función `verifyCardPIN()`

**Endpoint:** `http://192.168.1.249:8080/api/rfid-auth`

**Request JSON:**
```json
{
  "rfid_tag": "1A2B3C4D",
  "pin_ingresado": "1234",
  "accion": "verificar_pin"
}
```

**Validaciones en ESP32 ANTES de enviar:**
- ✅ UID no puede estar vacío
- ✅ PIN no puede estar vacío
- ✅ PIN mínimo 4 dígitos
- ✅ WiFi debe estar conectado

**Logging en Serial:**
```
[PIN] Enviando verificación: {...}
[PIN] UID: 1A2B3C4D | PIN: 1234
[PIN] URL destino: http://192.168.1.249:8080/api/rfid-auth
[PIN] Código respuesta: 200
[PIN] Respuesta JSON: {...}
```

---

### **PASO 4: Validación en Controlador Spring Boot**
**Archivo:** `RFIDAuthController.java` - Método `verificarPIN()`

**Validaciones en Controller:**
```java
- ✅ RFID tag no puede estar vacío
- ✅ PIN no puede estar vacío
- ✅ PIN mínimo 4 dígitos
```

**Respuestas HTTP:**
- `201 Created` → Tarjeta nueva registrada
- `200 OK` → Acceso autorizado o denegado
- `400 Bad Request` → Validación fallida
- `500 Internal Server Error` → Error en servidor

**Logging en Console:**
```
[CONTROLLER] Recibido - RFID: 1A2B3C4D, PIN: 1234
[CONTROLLER] Nueva tarjeta registrada: 1A2B3C4D
[CONTROLLER] Respuesta: true
```

---

### **PASO 5: Lógica de Negocio - Servicio**
**Archivo:** `RFIDAuthService.java` - Método `verificarORegistrarPIN()`

**Dos casos principales:**

#### **Caso A: TARJETA NUEVA**
```
1. Buscar RFID en BD
2. No existe → Crear registro nuevo
3. Guardar:
   - rfid_tag: "1A2B3C4D"
   - pin_acceso: "1234"
   - contador_accesos: 1
   - activo: true
   - fecha_creacion: NOW()
4. Retornar:
   {
     "autenticado": true,
     "es_nueva": true,
     "mensaje": "Tarjeta registrada con PIN exitosamente"
   }
```

**Logging en Console:**
```
[SERVICE] Tarjeta NUEVA encontrada. Registrando...
[SERVICE] ✓ Tarjeta registrada con ID: 1
[SERVICE] - RFID: 1A2B3C4D
[SERVICE] - PIN: 1234
[SERVICE] - Activo: true
```

#### **Caso B: TARJETA EXISTENTE**
```
1. Buscar RFID en BD
2. Existe → Verificar estado
3. Si NO está activa:
   - Retornar: "Tarjeta desactivada"
4. Si PIN coincide:
   - Actualizar: ultimo_acceso = NOW()
   - Incrementar: contador_accesos++
   - Retornar: "Acceso autorizado"
5. Si PIN NO coincide:
   - Retornar: "PIN incorrecto"
```

**Logging en Console:**
```
[SERVICE] Tarjeta EXISTENTE encontrada. Verificando PIN...
[SERVICE] - RFID BD: 1A2B3C4D
[SERVICE] - PIN BD: 1234
[SERVICE] ✓ PIN CORRECTO - Acceso autorizado
[SERVICE] - Accesos totales: 5
[SERVICE] - Último acceso: 2026-02-04T10:30:45
```

---

### **PASO 6: Guardado en Base de Datos**
**Archivo:** `database_setup.sql` - Tabla `rfid_accesos`

**Tabla en BD:**
```sql
CREATE TABLE rfid_accesos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rfid_tag VARCHAR(20) NOT NULL UNIQUE,
    nombre_usuario VARCHAR(100),
    pin_acceso VARCHAR(10),
    ultimo_acceso DATETIME,
    contador_accesos INT DEFAULT 1,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**Registro Guardado:**
```sql
INSERT INTO rfid_accesos (rfid_tag, pin_acceso, contador_accesos, activo, fecha_creacion)
VALUES ('1A2B3C4D', '1234', 1, TRUE, NOW());
```

**Verificación en BD:**
```sql
SELECT * FROM rfid_accesos WHERE rfid_tag = '1A2B3C4D';
```

---

### **PASO 7: Respuesta al ESP32**
**Archivo:** `ESP32_IoT_System.ino` - Función `verifyCardPIN()`

**Response JSON (éxito):**
```json
{
  "autenticado": true,
  "es_nueva": true,
  "mensaje": "Tarjeta registrada con PIN exitosamente",
  "rfid_tag": "1A2B3C4D"
}
```

**Response JSON (acceso denegado):**
```json
{
  "autenticado": false,
  "es_nueva": false,
  "mensaje": "PIN incorrecto",
  "rfid_tag": "1A2B3C4D"
}
```

**Visualización en LCD:**
- ✅ Acceso OK → "✓ ACCESO OK"
- ❌ Acceso denegado → "✗ DENEGADO"
- ⚠️ Error WiFi → "ERROR WiFi"
- ⏱️ Timeout → "TIMEOUT"

---

## 📊 DIAGRAMA DEL FLUJO

```
┌──────────────────────────────────────────────────────────────────────┐
│                          TARJETA RFID COLOCADA                        │
└──────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
        ┌──────────────────────────────────────────┐
        │ ESP32 Lee RFID RC522                    │
        │ UID: 1A2B3C4D                           │
        │ Pantalla LCD: "TARJETA: 1A2B3C4D"      │
        └──────────────────────────────────────────┘
                                    │
                                    ▼
        ┌──────────────────────────────────────────┐
        │ Solicitar PIN (Teclado 4x4)             │
        │ Mostrar: "PIN: ***"                      │
        │ Timeout: 30 segundos                     │
        └──────────────────────────────────────────┘
                                    │
                                    ▼
        ┌──────────────────────────────────────────┐
        │ Usuario ingresa PIN: "1234"              │
        │ Confirma con "#"                         │
        └──────────────────────────────────────────┘
                                    │
                                    ▼
        ┌──────────────────────────────────────────┐
        │ Validaciones en ESP32                    │
        │ ✅ UID no vacío                          │
        │ ✅ PIN no vacío                          │
        │ ✅ PIN >= 4 dígitos                      │
        │ ✅ WiFi conectado                        │
        └──────────────────────────────────────────┘
                                    │
                                    ▼
        ┌──────────────────────────────────────────┐
        │ POST JSON a API                          │
        │ URL: http://192.168.1.249:8080/api/rfid-auth
        │ Body: {"rfid_tag":"1A2B3C4D","pin_ingresado":"1234"}
        └──────────────────────────────────────────┘
                                    │
                                    ▼
        ┌──────────────────────────────────────────┐
        │ Spring Boot - RFIDAuthController         │
        │ Validaciones                             │
        │ ✅ RFID no vacío                         │
        │ ✅ PIN no vacío                          │
        │ ✅ PIN >= 4 dígitos                      │
        └──────────────────────────────────────────┘
                                    │
                                    ▼
        ┌──────────────────────────────────────────┐
        │ RFIDAuthService.verificarORegistrarPIN() │
        │ Buscar: SELECT * FROM rfid_accesos      │
        │ WHERE rfid_tag = '1A2B3C4D'             │
        └──────────────────────────────────────────┘
                                    │
                ┌───────────────────┴───────────────────┐
                │                                       │
                ▼                                       ▼
    ┌────────────────────────────┐      ┌────────────────────────────┐
    │ TARJETA NUEVA              │      │ TARJETA EXISTENTE          │
    │ INSERT INTO rfid_accesos    │      │ Verificar PIN              │
    │ - rfid_tag: 1A2B3C4D       │      │ - PIN correcto? ✅         │
    │ - pin_acceso: 1234         │      │ UPDATE ultimo_acceso       │
    │ - contador: 1              │      │ - PIN incorrecto? ❌       │
    │ - activo: true             │      │ - Tarjeta inactiva? ❌    │
    │ - fecha_creacion: NOW()    │      └────────────────────────────┘
    │                            │
    │ Respuesta: 201 Created     │      Respuesta: 200 OK
    │ es_nueva: true             │      es_nueva: false
    └────────────────────────────┘
                │                                       │
                └───────────────────┬───────────────────┘
                                    │
                                    ▼
        ┌──────────────────────────────────────────┐
        │ JSON Response a ESP32                     │
        │ {                                         │
        │   "autenticado": true/false,             │
        │   "es_nueva": true/false,                │
        │   "mensaje": "...",                      │
        │   "rfid_tag": "1A2B3C4D"                │
        │ }                                         │
        └──────────────────────────────────────────┘
                                    │
                                    ▼
        ┌──────────────────────────────────────────┐
        │ ESP32 Procesa Respuesta                  │
        │ Si autenticado == true:                  │
        │   "✓ ACCESO OK"                         │
        │   delay(3000)                            │
        │ Si autenticado == false:                 │
        │   "✗ DENEGADO"                          │
        │   delay(3000)                            │
        └──────────────────────────────────────────┘
                                    │
                                    ▼
        ┌──────────────────────────────────────────┐
        │ Limpieza de Variables                    │
        │ - waitingForPIN = false                  │
        │ - cardPinInput = ""                      │
        │ - currentCardUID = ""                    │
        │ - Volver a pantalla normal               │
        └──────────────────────────────────────────┘
```

---

## 🔍 VERIFICA EL FLUJO

### **En Serial Monitor del ESP32:**
```
[RFID] Tarjeta detectada: 1A2B3C4D
[PIN] Esperando entrada de PIN...
[PIN] Tecla presionada: 1
[PIN] Tecla presionada: 2
[PIN] Tecla presionada: 3
[PIN] Tecla presionada: 4
[PIN] Confirmando PIN...
[PIN] Enviando verificación: {"rfid_tag":"1A2B3C4D","pin_ingresado":"1234","accion":"verificar_pin"}
[PIN] UID: 1A2B3C4D | PIN: 1234
[PIN] URL destino: http://192.168.1.249:8080/api/rfid-auth
[PIN] Código respuesta: 201
[PIN] Respuesta JSON: {"autenticado":true,"es_nueva":true,"mensaje":"Tarjeta registrada con PIN exitosamente","rfid_tag":"1A2B3C4D"}
[PIN] ✓ ACCESO PERMITIDO - Guardando en BD
```

### **En Consola de Spring Boot:**
```
[CONTROLLER] Recibido - RFID: 1A2B3C4D, PIN: 1234
[SERVICE] verificarORegistrarPIN - RFID: 1A2B3C4D, PIN: 1234
[SERVICE] Tarjeta NUEVA encontrada. Registrando...
[SERVICE] ✓ Tarjeta registrada con ID: 1
[SERVICE] - RFID: 1A2B3C4D
[SERVICE] - PIN: 1234
[SERVICE] - Activo: true
[CONTROLLER] Nueva tarjeta registrada: 1A2B3C4D
```

### **En Base de Datos MySQL:**
```sql
SELECT * FROM rfid_accesos;

id | rfid_tag | nombre_usuario | pin_acceso | ultimo_acceso       | contador_accesos | activo | fecha_creacion
1  | 1A2B3C4D | NULL           | 1234       | 2026-02-04 10:30:45 | 1                | 1      | 2026-02-04 10:30:45
```

---

## ⚠️ POSIBLES PROBLEMAS Y SOLUCIONES

### **Problema 1: "ERROR WiFi" en LCD**
**Causa:** ESP32 no está conectado a WiFi
**Solución:**
- Verificar SSID y contraseña en línea 19-20 del `.ino`
- Verificar que el router está funcionando
- Verificar IP fija: 192.168.1.250

### **Problema 2: "TIMEOUT" en LCD**
**Causa:** No se confirmó PIN en 30 segundos
**Solución:**
- Presionar "#" para confirmar PIN antes de timeout
- Volver a colocar tarjeta si se agota el tiempo

### **Problema 3: "ERROR 400" en LCD**
**Causa:** Request JSON inválido
**Solución:**
- Verificar formato JSON en ESP32
- Verificar que RFID y PIN no están vacíos
- Verificar que PIN tiene mínimo 4 dígitos

### **Problema 4: "ERROR 500" en LCD**
**Causa:** Error en servidor Spring Boot
**Solución:**
- Revisar logs de consola de Spring Boot
- Verificar conexión a BD MySQL
- Verificar configuración en `application.properties`

### **Problema 5: PIN no se guarda en BD**
**Causa:** Hibernate no está guardando correctamente
**Solución:**
- Verificar que `spring.jpa.hibernate.ddl-auto=update`
- Verificar logs SQL en consola
- Ejecutar script `database_setup.sql` manualmente

---

## 🛠️ CONFIGURACIÓN REQUERIDA

### **ESP32:**
- ✅ SSID: "proyectoDAM"
- ✅ Password: "20260108"
- ✅ IP API: 192.168.1.249:8080
- ✅ Endpoint: /api/rfid-auth

### **Spring Boot:**
- ✅ server.port: 8080
- ✅ MySQL en localhost:3306
- ✅ BD: esp32_iot
- ✅ Usuario: root
- ✅ Contraseña: (vacía)

### **MySQL:**
- ✅ Tabla: rfid_accesos
- ✅ Índice: UNIQUE(rfid_tag)
- ✅ Charset: utf8mb4

---

## 📝 RESUMEN

| Paso | Componente | Acción | Resultado |
|------|-----------|--------|-----------|
| 1 | RFID RC522 | Lee tarjeta | UID: 1A2B3C4D |
| 2 | Teclado 4x4 | Ingresa PIN | PIN: 1234 |
| 3 | HTTP POST | Envía JSON | Status: 200/201 |
| 4 | Controller | Valida datos | ✅ Válidos |
| 5 | Service | Verifica lógica | ✅ Nueva tarjeta |
| 6 | BD MySQL | Guarda registro | ✅ Guardado |
| 7 | JSON Response | Retorna estado | ✅ Autenticado |
| 8 | LCD | Muestra resultado | "✓ ACCESO OK" |

---

## 🎯 PRÓXIMOS PASOS

1. ✅ Compilar y cargar código en ESP32
2. ✅ Iniciar servidor Spring Boot
3. ✅ Verificar conexión WiFi del ESP32
4. ✅ Colocar tarjeta RFID
5. ✅ Ingresar PIN y presionar "#"
6. ✅ Verificar respuesta en LCD
7. ✅ Confirmar guardado en BD con:
   ```sql
   SELECT * FROM rfid_accesos WHERE rfid_tag = '1A2B3C4D';
   ```

---

**Documento creado:** 2026-02-04  
**Versión:** 1.0  
**Estado:** ✅ Completo y listo para pruebas
