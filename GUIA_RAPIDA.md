# 🚀 GUÍA RÁPIDA DE INICIO

## 5 MINUTOS PARA COMENZAR

### **PASO 1: Preparar Hardware (2 min)**
✅ Conectar módulo RFID RC522 a ESP32  
✅ Conectar teclado 4x4  
✅ Conectar LCD I2C  
✅ Conectar DHT11 y sensor LDR  
✅ Conectar alimentación USB  

### **PASO 2: Cargar Código en ESP32 (1 min)**
```
1. Abrir Arduino IDE
2. Archivo → Abrir → ESP32_IoT_System.ino
3. Seleccionar: Herramientas → Placa → ESP32
4. Herramientas → Puerto → COM... (tu puerto)
5. Verificar (botón check)
6. Cargar (botón flecha)
```

### **PASO 3: Iniciar Spring Boot (1 min)**
```bash
cd GITAPI
mvn spring-boot:run
```

**Esperado en consola:**
```
Tomcat started on port 8080
```

### **PASO 4: Primera Prueba (1 min)**
```
1. Abrir Serial Monitor (Ctrl+Shift+M en Arduino IDE)
2. Colocar tarjeta RFID en lector
3. Ingresar PIN: 1234
4. Presionar "#"
5. Ver respuesta en LCD: "✓ ACCESO OK"
```

---

## ✅ CHECKLIST MÍNIMO

Antes de usar:

- [ ] Hardware conectado correctamente
- [ ] WiFi "proyectoDAM" disponible
- [ ] MySQL corriendo
- [ ] Spring Boot iniciado
- [ ] Código cargado en ESP32
- [ ] Serial Monitor abierto

---

## 📱 PANTALLAS LCD ESPERADAS

### **Al iniciar:**
```
"ESP32 IoT Ready"
"Conectando..."
        ↓
"WiFi OK"
"192.168.1.250"
        ↓
"T:24C H:65%"
"Esperando..."
```

### **Al detectar tarjeta:**
```
"TARJETA:"
"1A2B3C4D"
        ↓
"PIN:"
"# confirmar * canc"
```

### **Éxito:**
```
"✓ ACCESO OK"
"Nueva tarjeta!" o "Bienvenido!"
```

### **Error:**
```
"✗ DENEGADO"
"PIN incorrecto"
```

---

## 🔗 CONEXIONES RÁPIDAS

### **RFID RC522 → ESP32**
```
VCC → 3.3V
GND → GND
SCK → 18
MISO → 19
MOSI → 23
SS → 5
RST → 22
```

### **LCD I2C → ESP32**
```
VCC → 5V
GND → GND
SDA → 21
SCL → 15
```

### **Teclado 4x4 → ESP32**
```
Filas: 13, 12, 14, 25
Cols: 33, 32, 16, 17
```

### **DHT11 → ESP32**
```
VCC → 5V
GND → GND
DATA → 26
```

### **LDR → ESP32**
```
VCC → 5V
GND → GND
OUT → 27
```

---

## 🎯 RESULTADO ESPERADO

**Después de 5 minutos:**

1. ✅ LCD muestra temperatura/humedad
2. ✅ Serial Monitor muestra logs
3. ✅ Tarjeta RFID es detectada
4. ✅ LCD solicita PIN
5. ✅ PIN es enviado a API
6. ✅ Tarjeta es guardada en BD

---

## 🆘 SI NO FUNCIONA

### **"ERROR WiFi"**
- Verificar contraseña: "20260108"
- Reiniciar router
- Reiniciar ESP32

### **"ERROR" en LCD**
- Ver Serial Monitor
- Buscar logs con "ERROR"
- Revisar CHECKLIST_PRUEBAS.md

### **Pin no se guarda**
- Verificar MySQL: `SHOW DATABASES;`
- Verificar Spring Boot consola
- Revisar application.properties

---

## 📊 VERIFICAR EN BD

```sql
-- Conectar a MySQL
mysql -u root

-- Ver datos
USE esp32_iot;
SELECT * FROM rfid_accesos;

-- Debe mostrar tu tarjeta registrada
```

---

## 📚 DOCUMENTACIÓN COMPLETA

Después de primeras pruebas, leer:

1. [FLUJO_RFID_PIN_API_BD.md](FLUJO_RFID_PIN_API_BD.md) - Flujo completo
2. [CHECKLIST_PRUEBAS.md](CHECKLIST_PRUEBAS.md) - Todas las pruebas
3. [VERIFICACION_BD.sql](VERIFICACION_BD.sql) - Queries útiles
4. [RESUMEN_MEJORAS.md](RESUMEN_MEJORAS.md) - Cambios realizados

---

## 🎬 COMANDOS ÚTILES

### **Arduino IDE - Serial Monitor**
```
Ctrl+Shift+M → Abre Serial Monitor
115200 → Velocidad en bauds
Ctrl+L → Limpia pantalla
```

### **Spring Boot**
```
Ctrl+C → Detiene servidor
mvn clean → Limpia
mvn compile → Compila
mvn spring-boot:run → Inicia
```

### **MySQL**
```bash
mysql -u root
mysql> USE esp32_iot;
mysql> SELECT * FROM rfid_accesos;
mysql> \q
```

---

## ⏱️ TIEMPOS

| Actividad | Tiempo |
|-----------|--------|
| Hardware | 10 min |
| Cargar código | 3 min |
| Iniciar Spring Boot | 1 min |
| Primera prueba | 2 min |
| **Total** | **~16 min** |

---

## 💡 TIPS

✅ Verificar Serial Monitor mientras pruebas  
✅ El PIN se muestra como **** en LCD  
✅ Presionar "#" para confirmar  
✅ Presionar "*" para cancelar  
✅ Timeout: 30 segundos para ingresar PIN  
✅ WiFi se reconecta automáticamente  

---

## 🎓 PRÓXIMAS MEJORAS

1. Encriptar PIN (BCrypt)
2. Usar HTTPS/SSL
3. Autenticación JWT en API
4. Historial de accesos
5. Notificaciones por SMS/Email
6. Dashboard web

---

**¡Listo! Tu sistema RFID + PIN está funcionando.**

Cualquier problema: revisar los 3 archivos de documentación.

---

Última actualización: 2026-02-04
