# ✅ CHECKLIST DE CONFIGURACIÓN Y VERIFICACIÓN

## 📋 ANTES DE HACER PRUEBAS

### 1. HARDWARE ESP32
- [ ] Módulo RFID RC522 conectado a pines correctos
  - [ ] SDA (SS) → Pin 5
  - [ ] RST → Pin 22
  - [ ] SCK → Pin 18
  - [ ] MISO → Pin 19
  - [ ] MOSI → Pin 23
  - [ ] GND → GND
  - [ ] VCC → 3.3V

- [ ] Teclado 4x4 conectado a pines correctos
  - [ ] Filas → Pines 13, 12, 14, 25
  - [ ] Columnas → Pines 33, 32, 16, 17

- [ ] LCD I2C conectado a pines correctos
  - [ ] SDA → Pin 21
  - [ ] SCL → Pin 15
  - [ ] Dirección I2C: 0x27

- [ ] Sensor DHT11 conectado a Pin 26
- [ ] Sensor de luz (LDR) conectado a Pin 27
- [ ] Cable USB para alimentación
- [ ] Antena externa del RFID (si aplica)

### 2. RED WIFI
- [ ] Red WIFI "proyectoDAM" disponible
- [ ] Contraseña: "20260108"
- [ ] ESP32 puede conectarse a la red
- [ ] IP Estática asignada: 192.168.1.250

### 3. SERVIDOR SPRING BOOT
- [ ] Java JDK instalado (mínimo JDK 11)
- [ ] Maven instalado
- [ ] Código compilado: `mvn clean package`
- [ ] Servidor en puerto: 8080
- [ ] Accesible desde ESP32: `http://192.168.1.249:8080`

### 4. BASE DE DATOS MySQL
- [ ] MySQL Server iniciado
- [ ] Base de datos "esp32_iot" creada
- [ ] Tabla "rfid_accesos" creada
- [ ] Usuario "root" sin contraseña (o contraseña configurada)
- [ ] Script "database_setup.sql" ejecutado

### 5. CÓDIGO ESP32
- [ ] Archivo "ESP32_IoT_System.ino" actualizado
- [ ] Validaciones de WiFi agregadas
- [ ] Validaciones de PIN agregadas
- [ ] Validaciones de UID agregadas
- [ ] Librerías instaladas:
  - [ ] Wire.h (I2C)
  - [ ] LiquidCrystal_I2C.h
  - [ ] WiFi.h
  - [ ] HTTPClient.h
  - [ ] SPI.h
  - [ ] MFRC522.h
  - [ ] DHT.h
  - [ ] ArduinoJson.h

### 6. CÓDIGO SPRING BOOT
- [ ] RFIDAuthController.java actualizado con validaciones
- [ ] RFIDAuthService.java actualizado con logs
- [ ] application.properties configurado correctamente:
  - [ ] spring.datasource.url=jdbc:mysql://localhost:3306/esp32_iot
  - [ ] spring.datasource.username=root
  - [ ] spring.jpa.hibernate.ddl-auto=update

---

## 🔄 DURANTE LAS PRUEBAS

### PRUEBA 1: Conexión WiFi
```
Esperado en Serial Monitor:
[WiFi] Conectando a: proyectoDAM
[WiFi] CONECTADO!
[WiFi] IP: 192.168.1.250
```
- [ ] ESP32 se conecta a WiFi
- [ ] IP asignada correctamente
- [ ] LCD muestra "WiFi OK"

### PRUEBA 2: Lectura de RFID
```
Acciones:
1. Colocar tarjeta en lector
2. Ver Serial Monitor

Esperado:
[RFID] Tarjeta detectada: 1A2B3C4D
[PIN] Esperando entrada de PIN...
LCD: "TARJETA: 1A2B3C4D"
```
- [ ] Tarjeta es detectada
- [ ] UID aparece en Serial
- [ ] LCD muestra UID correctamente
- [ ] Sistema entra en modo espera de PIN

### PRUEBA 3: Ingreso de PIN
```
Acciones:
1. Presionar teclas: 1, 2, 3, 4
2. Presionar "#" para confirmar
3. Ver Serial Monitor

Esperado:
[PIN] Tecla presionada: 1
[PIN] Tecla presionada: 2
[PIN] Tecla presionada: 3
[PIN] Tecla presionada: 4
[PIN] Confirmando PIN...
LCD: "PIN: ****" → "PIN: ****" (4 asteriscos)
```
- [ ] Cada tecla registra correctamente
- [ ] LCD muestra asteriscos (no el PIN real)
- [ ] Se puede confirmar con "#"
- [ ] Se puede cancelar con "*"

### PRUEBA 4: Validación de PIN
```
Esperado si PIN < 4 dígitos:
[PIN] ERROR: PIN muy corto (mínimo 4 dígitos)
LCD: "PIN muy corto"
```
- [ ] Sistema rechaza PIN < 4 dígitos
- [ ] Se vuelve a pedir PIN

### PRUEBA 5: Envío por API
```
Esperado en Serial Monitor:
[PIN] Enviando verificación: {"rfid_tag":"1A2B3C4D","pin_ingresado":"1234","accion":"verificar_pin"}
[PIN] URL destino: http://192.168.1.249:8080/api/rfid-auth
[PIN] Código respuesta: 201
[PIN] Respuesta JSON: {"autenticado":true,"es_nueva":true,...}
```
- [ ] JSON es válido
- [ ] URL es correcta
- [ ] Respuesta HTTP es 200 o 201
- [ ] JSON response es parseado correctamente

### PRUEBA 6: Primera Tarjeta (Nueva)
```
Acciones:
1. Colocar tarjeta NUEVA (nunca registrada)
2. Ingresar PIN: 1234
3. Presionar "#"

Esperado en Serial Monitor:
[SERVICE] Tarjeta NUEVA encontrada. Registrando...
[SERVICE] ✓ Tarjeta registrada con ID: 1
[SERVICE] - RFID: 1A2B3C4D
[SERVICE] - PIN: 1234

Esperado en LCD:
"✓ ACCESO OK"
"Nueva tarjeta!"
```
- [ ] Sistema reconoce nueva tarjeta
- [ ] PIN se guarda en BD
- [ ] Respuesta HTTP es 201 (Created)
- [ ] LCD muestra "ACCESO OK"

### PRUEBA 7: Tarjeta Existente (PIN Correcto)
```
Acciones:
1. Volver a colocar misma tarjeta
2. Ingresar PIN: 1234 (el correcto)
3. Presionar "#"

Esperado en Serial Monitor:
[SERVICE] Tarjeta EXISTENTE encontrada. Verificando PIN...
[SERVICE] ✓ PIN CORRECTO - Acceso autorizado
[SERVICE] - Accesos totales: 2

Esperado en LCD:
"✓ ACCESO OK"
"Bienvenido!"
```
- [ ] Sistema verifica tarjeta existente
- [ ] PIN se compara correctamente
- [ ] contador_accesos se incrementa
- [ ] Respuesta HTTP es 200 (OK)
- [ ] LCD muestra "ACCESO OK"

### PRUEBA 8: Tarjeta Existente (PIN Incorrecto)
```
Acciones:
1. Colocar tarjeta registrada
2. Ingresar PIN incorrecto: 5678
3. Presionar "#"

Esperado en Serial Monitor:
[SERVICE] Tarjeta EXISTENTE encontrada. Verificando PIN...
[SERVICE] ✗ PIN INCORRECTO
[SERVICE] - PIN esperado: 1234
[SERVICE] - PIN recibido: 5678

Esperado en LCD:
"✗ DENEGADO"
"PIN incorrecto"
```
- [ ] Sistema rechaza PIN incorrecto
- [ ] LCD muestra "DENEGADO"
- [ ] Respuesta JSON: "autenticado": false

### PRUEBA 9: Base de Datos
```
Acciones:
1. Ejecutar en MySQL:
SELECT * FROM rfid_accesos;

Esperado:
id | rfid_tag | pin_acceso | contador_accesos | activo | fecha_creacion
1  | 1A2B3C4D | 1234       | 2                | 1      | 2026-02-04 10:30:45
```
- [ ] Tarjeta existe en BD
- [ ] PIN está guardado (hash o encriptado sería mejor)
- [ ] contador_accesos se incrementó
- [ ] último_acceso se actualizó
- [ ] activo = 1

### PRUEBA 10: Desconexión WiFi
```
Acciones:
1. Desconectar WiFi (apagar router)
2. Colocar tarjeta
3. Intentar acceder

Esperado en Serial Monitor:
[PIN] ERROR: WiFi desconectado

Esperado en LCD:
"ERROR WiFi"
"Reconectando..."
```
- [ ] Sistema detecta WiFi desconectado
- [ ] Intenta reconectarse
- [ ] No envía datos a API

---

## 🔐 SEGURIDAD (Mejoras Futuras)

- [ ] ⚠️ PIN se guarda en TEXTO PLANO (usar HASH!)
- [ ] ⚠️ Comunicación sin HTTPS (usar SSL/TLS)
- [ ] ⚠️ Credenciales en código (usar variables de entorno)
- [ ] ⚠️ Sin autenticación en API (agregar JWT)
- [ ] ⚠️ Sin validación de origen CORS (restringir)

**RECOMENDACIÓN:** Encriptar PIN con BCrypt antes de guardar:
```java
String pinEncriptado = BCryptPasswordEncoder.encode(pin);
```

---

## 📊 MÉTRICAS DE PRUEBA

| Prueba | Estado | Notas |
|--------|--------|-------|
| WiFi | ✅/❌ | |
| RFID | ✅/❌ | |
| Teclado | ✅/❌ | |
| API POST | ✅/❌ | |
| BD Guardado | ✅/❌ | |
| PIN Correcto | ✅/❌ | |
| PIN Incorrecto | ✅/❌ | |
| Tarjeta Nueva | ✅/❌ | |
| Tarjeta Existente | ✅/❌ | |
| Error Handling | ✅/❌ | |

---

## 🆘 TROUBLESHOOTING

### Problema: "ERROR WiFi" en LCD
```
1. Verificar SSID: "proyectoDAM"
2. Verificar contraseña: "20260108"
3. Verificar que router está ON
4. Verificar signal WiFi strength
5. Reiniciar ESP32
```

### Problema: "ERROR 400" en LCD
```
1. Verificar Serial Monitor para ver JSON enviado
2. Verificar que RFID no está vacío
3. Verificar que PIN no está vacío
4. Verificar que PIN >= 4 dígitos
5. Revisar logs de Spring Boot
```

### Problema: "ERROR 500" en LCD
```
1. Revisar consola de Spring Boot
2. Verificar conexión a MySQL
3. Verificar que base de datos existe
4. Ejecutar: USE esp32_iot;
5. Ejecutar: SHOW TABLES;
```

### Problema: PIN no se guarda en BD
```
1. Verificar logs de Hibernate
2. Ejecutar: SHOW CREATE TABLE rfid_accesos;
3. Verificar que DDL auto está en "update"
4. Reiniciar Spring Boot
5. Intentar INSERT manual en MySQL
```

### Problema: RFID no detecta tarjeta
```
1. Verificar conexiones SPI
2. Verificar pines: SS=5, RST=22
3. Verificar antena del RC522
4. Probar en otra posición
5. Ver valor RSSI en Serial Monitor
```

---

## ✅ CHECKLIST FINAL

Antes de considerar el proyecto completado:

- [ ] WiFi conecta correctamente
- [ ] RFID detecta tarjetas
- [ ] Teclado registra entrada
- [ ] API recibe POST correctamente
- [ ] BD guarda tarjeta nueva
- [ ] BD verifica PIN existente
- [ ] PIN incorrecto es rechazado
- [ ] contador_accesos incrementa
- [ ] ultimo_acceso se actualiza
- [ ] LCD muestra respuestas correctas
- [ ] Serial Monitor muestra logs detallados
- [ ] MySQL contiene datos registrados

---

**Fecha:** 2026-02-04  
**Versión:** 1.0  
**Estado:** Listo para pruebas
