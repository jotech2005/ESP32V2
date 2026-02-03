// =====================================================
// ESP32 - SISTEMA IOT CON MÚLTIPLES SENSORES
// WiFi + RFID + DHT11 + LDR + Teclado + LCD I2C
// =====================================================

#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <SPI.h>
#include <MFRC522.h>
#include <DHT.h>
#include <ArduinoJson.h>

// =====================================================
// CONFIGURACIÓN WIFI
// =====================================================
const char* SSID = "proyectoDAM";
const char* PASS = "20260108";
const char* API_HOST = "192.168.1.249";
const int API_PORT = 8080;
const char* API_ENDPOINT = "/api/sensor-data"; // Ajusta según tu ruta en SpringBoot

// IP fija ESP32 = 192.168.1.250
IPAddress local_IP(192, 168, 1, 250);
IPAddress gateway(192, 168, 1, 1);
IPAddress subnet(255, 255, 255, 0);
IPAddress dns(192, 168, 1, 1);

// =====================================================
// CONFIGURACIÓN LCD I2C
// =====================================================
#define LCD_ADDR 0x27  // Dirección I2C (0x3F también es común)
#define LCD_COLS 13
#define LCD_ROWS 2
LiquidCrystal_I2C lcd(LCD_ADDR, LCD_COLS, LCD_ROWS);

// =====================================================
// CONFIGURACIÓN TECLADO 4x4
// =====================================================
#define ROWS 4
#define COLS 4

// Pines de las filas (salida)
int rowPins[ROWS] = {13, 12, 14, 25};

// Pines de las columnas (entrada)
int colPins[COLS] = {33, 32, 16, 17};

char keys[ROWS][COLS] = {
  {'1', '2', '3', 'A'},
  {'4', '5', '6', 'B'},
  {'7', '8', '9', 'C'},
  {'*', '0', '#', 'D'}
};

String keypadInput = "";

// =====================================================
// CONFIGURACIÓN RFID RC522
// =====================================================
#define RFID_SS 5      // SDA/SS
#define RFID_RST 22    // RST
MFRC522 rfid(RFID_SS, RFID_RST);
String lastRFIDTag = "";
unsigned long lastRFIDTime = 0;
const unsigned long RFID_COOLDOWN = 2000; // 2 segundos entre lecturas

// =====================================================
// CONFIGURACIÓN DHT11
// =====================================================
#define DHT_PIN 26
#define DHT_TYPE DHT11
DHT dht(DHT_PIN, DHT_TYPE);
float temperature = 0.0;
float humidity = 0.0;

// =====================================================
// CONFIGURACIÓN SENSOR DE LUZ (LDR)
// =====================================================
#define LIGHT_SENSOR_PIN 27
bool lightDetected = false;

// =====================================================
// VARIABLES GLOBALES
// =====================================================
unsigned long lastSendTime = 0;
const unsigned long SEND_INTERVAL = 10000; // 10 segundos
WiFiClient client;
HTTPClient http;

// Variables para control de PIN
String cardPinInput = "";
String currentCardUID = "";
bool waitingForPIN = false;
unsigned long pinEntryTimeout = 0;
const unsigned long PIN_TIMEOUT = 30000; // 30 segundos para ingresar PIN
const int MAX_PIN_LENGTH = 6;

// =====================================================
// SETUP INICIAL
// =====================================================
void setup() {
  Serial.begin(115200);
  delay(1000);
  
  Serial.println("\n\n=== INICIALIZANDO ESP32 ===");
  
  // Inicializar LCD
  initLCD();
  
  // Inicializar Teclado
  initKeypad();
  
  // Inicializar RFID
  initRFID();
  
  // Inicializar DHT11
  dht.begin();
  Serial.println("[DHT11] Inicializado");
  
  // Inicializar sensor de luz
  pinMode(LIGHT_SENSOR_PIN, INPUT);
  Serial.println("[LUZ] Inicializado");
  
  // Conectar WiFi
  connectToWiFi();
  
  Serial.println("=== SETUP COMPLETADO ===\n");
}

// =====================================================
// LOOP PRINCIPAL
// =====================================================
void loop() {
  // Verificar si estamos esperando PIN
  if (waitingForPIN) {
    // Verificar timeout
    if (millis() - pinEntryTimeout > PIN_TIMEOUT) {
      Serial.println("[PIN] Timeout - acceso cancelado");
      lcd.clear();
      lcd.print("TIMEOUT");
      lcd.setCursor(0, 1);
      lcd.print("Acceso cancelado");
      delay(2000);
      waitingForPIN = false;
      cardPinInput = "";
      currentCardUID = "";
      updateLCDDisplay();
    } else {
      // Leer teclado para PIN
      readKeypadForPIN();
    }
  } else {
    // Leer RFID normalmente
    readRFID();
    
    // Leer sensores
    readSensors();
    
    // Enviar datos cada 10 segundos
    if (millis() - lastSendTime >= SEND_INTERVAL) {
      sendDataToAPI();
      lastSendTime = millis();
    }
  }
  
  delay(50);
}

// =====================================================
// INICIALIZAR LCD I2C
// =====================================================
void initLCD() {
  Wire.begin(21, 15); // SDA=21, SCL=15
  lcd.init();
  lcd.backlight();
  lcd.print("ESP32 IoT Ready");
  lcd.setCursor(0, 1);
  lcd.print("Conectando...");
  Serial.println("[LCD] Inicializado en 0x27");
}

// =====================================================
// INICIALIZAR TECLADO 4x4
// =====================================================
void initKeypad() {
  // Configurar filas como salida
  for (int i = 0; i < ROWS; i++) {
    pinMode(rowPins[i], OUTPUT);
    digitalWrite(rowPins[i], HIGH);
  }
  
  // Configurar columnas como entrada
  for (int i = 0; i < COLS; i++) {
    pinMode(colPins[i], INPUT);
  }
  
  Serial.println("[KEYPAD] Inicializado");
}

// =====================================================
// LEER TECLADO 4x4
// =====================================================
void readKeypad() {
  for (int row = 0; row < ROWS; row++) {
    digitalWrite(rowPins[row], LOW);
    delay(5);
    
    for (int col = 0; col < COLS; col++) {
      if (digitalRead(colPins[col]) == LOW) {
        char key = keys[row][col];
        Serial.print("[KEYPAD] Tecla presionada: ");
        Serial.println(key);
        
        keypadInput += key;
        
        if (keypadInput.length() > 16) {
          keypadInput = keypadInput.substring(keypadInput.length() - 16);
        }
        
        updateLCDDisplay();
        delay(200); // Debounce
      }
    }
    
    digitalWrite(rowPins[row], HIGH);
  }
}

// =====================================================
// LEER TECLADO PARA PIN
// =====================================================
void readKeypadForPIN() {
  for (int row = 0; row < ROWS; row++) {
    digitalWrite(rowPins[row], LOW);
    delay(5);
    
    for (int col = 0; col < COLS; col++) {
      if (digitalRead(colPins[col]) == LOW) {
        char key = keys[row][col];
        Serial.print("[PIN] Tecla presionada: ");
        Serial.println(key);
        
        delay(200); // Debounce
        
        // Procesar la tecla
        if (key == '#') {  // Confirmar PIN
          if (cardPinInput.length() >= 4) {
            Serial.println("[PIN] Confirmando PIN...");
            verifyCardPIN();
          } else {
            Serial.println("[PIN] PIN muy corto (mínimo 4 dígitos)");
            lcd.clear();
            lcd.print("PIN corto");
            lcd.setCursor(0, 1);
            lcd.print("Min 4 digitos");
            delay(1500);
            displayPINEntry();
          }
        } 
        else if (key == '*') {  // Cancelar
          Serial.println("[PIN] Acceso cancelado por usuario");
          lcd.clear();
          lcd.print("CANCELADO");
          delay(2000);
          waitingForPIN = false;
          cardPinInput = "";
          currentCardUID = "";
          updateLCDDisplay();
        }
        else if (cardPinInput.length() < MAX_PIN_LENGTH) {  // Agregar dígito
          cardPinInput += key;
          displayPINEntry();
        }
      }
    }
    
    digitalWrite(rowPins[row], HIGH);
  }
}

// =====================================================
// MOSTRAR PANTALLA DE ENTRADA DE PIN
// =====================================================
void displayPINEntry() {
  lcd.clear();
  lcd.print("PIN:");
  
  // Mostrar asteriscos en lugar del PIN real
  String pinDisplay = "";
  for (int i = 0; i < cardPinInput.length(); i++) {
    pinDisplay += "*";
  }
  lcd.print(pinDisplay);
  
  lcd.setCursor(0, 1);
  lcd.print("# confirmar");
  if (cardPinInput.length() > 0) {
    lcd.print(" * canc");
  }
}

// =====================================================
// INICIALIZAR RFID
// =====================================================
void initRFID() {
  SPI.begin(18, 19, 23, 5); // SCK=18, MISO=19, MOSI=23, SS=5
  rfid.PCD_Init();
  Serial.println("[RFID] Inicializado en SPI (SS=5, RST=22)");
}

// =====================================================
// LEER RFID
// =====================================================
void readRFID() {
  if (!rfid.PICC_IsNewCardPresent() || !rfid.PICC_ReadCardSerial()) {
    return;
  }
  
  // Evitar lecturas duplicadas
  if (millis() - lastRFIDTime < RFID_COOLDOWN) {
    return;
  }
  
  lastRFIDTime = millis();
  
  // Construir UID como string
  String rfidTag = "";
  for (byte i = 0; i < rfid.uid.size; i++) {
    if (rfid.uid.uidByte[i] < 0x10) {
      rfidTag += "0";
    }
    rfidTag += String(rfid.uid.uidByte[i], HEX);
  }
  rfidTag.toUpperCase();
  
  lastRFIDTag = rfidTag;
  Serial.print("[RFID] Tarjeta detectada: ");
  Serial.println(rfidTag);
  
  // Guardar UID para verificación de PIN
  currentCardUID = rfidTag;
  
  // Mostrar en LCD
  lcd.clear();
  lcd.print("TARJETA:");
  lcd.setCursor(0, 1);
  lcd.print(rfidTag.substring(0, 13));
  delay(2000);
  
  // Solicitar PIN
  waitingForPIN = true;
  cardPinInput = "";
  pinEntryTimeout = millis();
  
  Serial.println("[PIN] Esperando entrada de PIN...");
  displayPINEntry();
  
  rfid.PICC_HaltA();
  rfid.PCD_StopCrypto1();
}

// =====================================================
// VERIFICAR PIN DE TARJETA
// =====================================================
void verifyCardPIN() {
  // Enviar solicitud al servidor para verificar PIN
  String jsonPayload;
  StaticJsonDocument<256> doc;
  doc["rfid_tag"] = currentCardUID;
  doc["pin_ingresado"] = cardPinInput;
  doc["accion"] = "verificar_pin";
  
  serializeJson(doc, jsonPayload);
  
  Serial.print("[PIN] Enviando verificación: ");
  Serial.println(jsonPayload);
  
  if (WiFi.status() == WL_CONNECTED) {
    String url = "http://" + String(API_HOST) + ":" + String(API_PORT) + "/api/rfid-auth";
    
    http.begin(client, url);
    http.addHeader("Content-Type", "application/json");
    
    int httpResponseCode = http.POST(jsonPayload);
    
    Serial.print("[PIN] Código respuesta: ");
    Serial.println(httpResponseCode);
    
    if (httpResponseCode == 200) {
      String response = http.getString();
      
      // Parsear respuesta
      StaticJsonDocument<256> responseDoc;
      deserializeJson(responseDoc, response);
      
      bool autenticado = responseDoc["autenticado"] | false;
      bool esNuevaTargeta = responseDoc["es_nueva"] | false;
      
      Serial.print("[PIN] Respuesta: ");
      Serial.println(response);
      
      if (autenticado) {
        // Acceso permitido
        Serial.println("[PIN] ✓ ACCESO PERMITIDO");
        lcd.clear();
        lcd.print("✓ ADELANTE");
        if (esNuevaTargeta) {
          lcd.setCursor(0, 1);
          lcd.print("PIN registrado");
        } else {
          lcd.setCursor(0, 1);
          lcd.print("Bienvenido!");
        }
        delay(3000);
      } else {
        // Acceso denegado
        Serial.println("[PIN] ✗ ACCESO DENEGADO");
        lcd.clear();
        lcd.print("✗ ACCESO DENEGADO");
        lcd.setCursor(0, 1);
        lcd.print("PIN incorrecto");
        delay(3000);
      }
    } else {
      // Error en comunicación
      Serial.print("[PIN] Error en servidor: ");
      Serial.println(http.errorToString(httpResponseCode));
      lcd.clear();
      lcd.print("ERROR");
      lcd.setCursor(0, 1);
      lcd.print("Sin conexion");
      delay(2000);
    }
    
    http.end();
  } else {
    Serial.println("[PIN] WiFi desconectado");
    lcd.clear();
    lcd.print("ERROR WiFi");
    delay(2000);
  }
  
  // Limpiar variables
  waitingForPIN = false;
  cardPinInput = "";
  currentCardUID = "";
  updateLCDDisplay();
}

// =====================================================
// LEER SENSORES DHT11 Y LUZ
// =====================================================
void readSensors() {
  // DHT11
  temperature = dht.readTemperature();
  humidity = dht.readHumidity();
  
  if (isnan(temperature) || isnan(humidity)) {
    Serial.println("[DHT11] Error al leer sensor");
    temperature = 0.0;
    humidity = 0.0;
  } else {
    Serial.print("[DHT11] Temp: ");
    Serial.print(temperature);
    Serial.print("C - Humedad: ");
    Serial.print(humidity);
    Serial.println("%");
  }
  
  // Sensor de luz
  lightDetected = digitalRead(LIGHT_SENSOR_PIN) == HIGH;
  Serial.print("[LUZ] Detectada: ");
  Serial.println(lightDetected ? "SI" : "NO");
}

// =====================================================
// ACTUALIZAR DISPLAY LCD
// =====================================================
void updateLCDDisplay() {
  lcd.clear();
  
  // Primera línea: Temperatura y Humedad
  lcd.setCursor(0, 0);
  lcd.print("T:");
  lcd.print((int)temperature);
  lcd.print("C H:");
  lcd.print((int)humidity);
  lcd.print("%");
  
  // Segunda línea: Entrada teclado
  lcd.setCursor(0, 1);
  if (keypadInput.length() > 0) {
    String displayText = keypadInput.length() > 16 ? 
                         keypadInput.substring(keypadInput.length() - 16) : 
                         keypadInput;
    lcd.print(displayText);
  } else {
    lcd.print("Esperando...");
  }
}

// =====================================================
// CONECTAR A WIFI
// =====================================================
void connectToWiFi() {
  Serial.print("[WiFi] Conectando a: ");
  Serial.println(SSID);
  
  if (!WiFi.config(local_IP, gateway, subnet, dns)) {
    Serial.println("[WiFi] Error configurando IP fija");
  }

  WiFi.begin(SSID, PASS);
  
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 40) {
    delay(500);
    Serial.print(".");
    attempts++;
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n[WiFi] CONECTADO!");
    Serial.print("[WiFi] IP: ");
    Serial.println(WiFi.localIP());
    
    lcd.clear();
    lcd.print("WiFi OK");
    lcd.setCursor(0, 1);
    lcd.print(WiFi.localIP());
    delay(2000);
  } else {
    Serial.println("\n[WiFi] ERROR - No conectado");
    lcd.clear();
    lcd.print("WiFi FALLO");
    delay(2000);
  }
}

// =====================================================
// ENVIAR DATOS A API (JSON)
// =====================================================
void sendDataToAPI() {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("[API] WiFi desconectado, reconectando...");
    connectToWiFi();
    return;
  }
  
  // Crear JSON
  StaticJsonDocument<512> jsonDoc;
  jsonDoc["timestamp"] = millis();
  jsonDoc["temperatura"] = temperature;
  jsonDoc["humedad"] = humidity;
  jsonDoc["luz_detectada"] = lightDetected;
  jsonDoc["teclado_input"] = keypadInput;
  jsonDoc["ultima_tarjeta_rfid"] = lastRFIDTag;
  jsonDoc["esp32_ip"] = WiFi.localIP().toString();
  jsonDoc["rssi"] = WiFi.RSSI(); // Intensidad de señal WiFi
  
  String jsonString;
  serializeJson(jsonDoc, jsonString);
  
  // Enviar POST a SpringBoot
  String url = "http://" + String(API_HOST) + ":" + String(API_PORT) + String(API_ENDPOINT);
  
  Serial.print("[API] Enviando a: ");
  Serial.println(url);
  Serial.print("[API] JSON: ");
  Serial.println(jsonString);
  
  http.begin(client, url);
  http.addHeader("Content-Type", "application/json");
  
  int httpResponseCode = http.POST(jsonString);
  
  Serial.print("[API] Código respuesta: ");
  Serial.println(httpResponseCode);
  
  if (httpResponseCode > 0) {
    String response = http.getString();
    Serial.print("[API] Respuesta: ");
    Serial.println(response);
  } else {
    Serial.print("[API] Error: ");
    Serial.println(http.errorToString(httpResponseCode));
  }
  
  http.end();
}

// =====================================================
// FIN DEL CÓDIGO
// =====================================================
