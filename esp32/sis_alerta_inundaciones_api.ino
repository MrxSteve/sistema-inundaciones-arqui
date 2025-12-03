#define TINY_GSM_MODEM_SIM800
#include <TinyGsmClient.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

// ======== CONFIG WIFI ========
const char* WIFI_SSID = "GalaxyNote10+4181";
const char* WIFI_PASSWORD = "ismael23@gh22";

// ======== API ========
const char* API_BASE_URL = "http://192.168.60.176:8085/api/alertas/esp32";
const char* API_NUMEROS_URL = "http://192.168.60.176:8085/api/esp32/numeros-sms";

// ======== NÚMEROS DE EMERGENCIA ========
const char* NUMEROS_EMERGENCIA[] = {
  "+50368553083",
  "+50361572882"
};
const int TOTAL_EMERGENCIA = 2;

// Números dinamicos de API
String numerosTelefono[20];
int totalNumeros = 0;

// ======== SIM800 ========
HardwareSerial SerialGSM(1);
TinyGsm modem(SerialGSM);

// ======== PINES ========
#define SIM800_RX 16
#define SIM800_TX 17
#define PIN_TRIG 26
#define PIN_ECHO 27
#define PIN_LED 33

// ======== SENSOR ========
#define DISTANCIA_ALERTA 20
#define DISTANCIA_MAXIMA 400
#define DISTANCIA_MINIMA 2
#define NUMERO_LECTURAS 5
#define LECTURAS_CONSECUTIVAS 3
#define TIMEOUT_SENSOR 30000
#define INTERVALO_LECTURA 500

// ======== INFO DISPOSITIVO ========
const String DISPOSITIVO_ID = "ESP32-SENSOR-001";
const String UBICACION = "Zona Norte - Río Principal";

// ======== VARIABLES ========
float lecturas[NUMERO_LECTURAS];
int indice_lectura = 0;
int contador_alarma = 0;
bool alarma_activa = false;
float ultima_distancia = 0;
float ultimo_promedio = 0;
unsigned long ultima_lectura = 0;

bool wifi_conectado = false;
bool gsm_conectado = false;

/************************************************************
 *  SETUP
 ************************************************************/
void setup() {
  Serial.begin(115200);
  delay(1000);

  pinMode(PIN_TRIG, OUTPUT);
  pinMode(PIN_ECHO, INPUT);
  pinMode(PIN_LED, OUTPUT);
  digitalWrite(PIN_LED, LOW);

  Serial.println("\n=== SISTEMA DE INUNDACIONES INICIADO ===");

  for (int i = 0; i < NUMERO_LECTURAS; i++)
    lecturas[i] = -1;

  inicializarWiFi();
  inicializarModem();

  if (obtenerNumerosDesdeLaAPI()) {
    Serial.println("Números obtenidos desde API:");
    for (int i = 0; i < totalNumeros; i++)
      Serial.println(" - " + numerosTelefono[i]);
  } else {
    Serial.println("No se pudieron obtener números desde API.");
  }

  Serial.println("\nSistema listo.\n");
}

/************************************************************
 *  LOOP
 ************************************************************/
void loop() {
  if (WiFi.status() != WL_CONNECTED && wifi_conectado) {
    wifi_conectado = false;
    Serial.println("WiFi desconectado, reintentando...");
    inicializarWiFi();
  }

  unsigned long t = millis();
  if (t - ultima_lectura >= INTERVALO_LECTURA) {
    ultima_lectura = t;
    float distancia = medirDistancia();

    if (distancia > 0) {
      procesarLectura(distancia);
      ultima_distancia = distancia;
    } else {
      Serial.println("Lectura inválida.");
    }
  }
}

/************************************************************
 *  WIFI
 ************************************************************/
void inicializarWiFi() {
  Serial.print("Conectando a WiFi");
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  int intentos = 0;
  while (WiFi.status() != WL_CONNECTED && intentos < 20) {
    delay(500);
    Serial.print(".");
    intentos++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    wifi_conectado = true;
    Serial.println("\nWiFi conectado!");
  } else {
    Serial.println("\nNo se pudo conectar a WiFi.");
  }
}

/************************************************************
 *  SIM800
 ************************************************************/
void inicializarModem() {
  Serial.println("\n--- Iniciando configuración del módem SIM800 ---");

  SerialGSM.begin(9600, SERIAL_8N1, SIM800_RX, SIM800_TX);
  delay(3000);

  Serial.println("Reiniciando módem...");
  modem.restart();
  String modemInfo = modem.getModemInfo();
  Serial.print("Info del Modem: ");
  Serial.println(modemInfo);

  Serial.print("Esperando red GSM (2G)...");
  if (!modem.waitForNetwork()) {
    Serial.println(" ERROR: No se detectó red GSM.");
    gsm_conectado = false;
    return;
  }
  Serial.println(" Red GSM detectada.");
  gsm_conectado = true;
}

/************************************************************
 *  OBTENER NÚMEROS DESDE API
 ************************************************************/
bool obtenerNumerosDesdeLaAPI() {
  if (!wifi_conectado) return false;

  HTTPClient http;
  http.begin(API_NUMEROS_URL);
  http.addHeader("Content-Type", "application/json");

  int code = http.GET();
  if (code != 200) {
    http.end();
    return false;
  }

  String json = http.getString();
  http.end();

  DynamicJsonDocument doc(2048);
  deserializeJson(doc, json);

  JsonArray arr = doc["numeros"].as<JsonArray>();
  if (arr.isNull()) return false;

  totalNumeros = 0;
  for (JsonVariant v : arr) {
    if (totalNumeros < 20) {
      numerosTelefono[totalNumeros] = v.as<String>();
      totalNumeros++;
    }
  }

  return true;
}

/************************************************************
 *  ENVÍO DE SMS (API + EMERGENCIA)
 ************************************************************/
void enviarSMS(String mensaje) {
  Serial.println("\n--- ENVIANDO SMS ---");

  // 1. Enviar a números API (si los hay)
  if (totalNumeros > 0) {
    Serial.println("Enviando a números de API:");
    for (int i = 0; i < totalNumeros; i++) {
      const char* numero = numerosTelefono[i].c_str();

      Serial.print("Enviando a ");
      Serial.print(numero);

      bool enviado = modem.sendSMS(numero, mensaje);

      if (enviado) {
        Serial.println(" -> OK");
      } else {
        Serial.println(" -> FALLÓ (Saldo o cobertura).");
      }
      delay(500); // Pausa entre envíos
    }
  }

  // 2. Enviar SIEMPRE a números de emergencia
  Serial.println("Enviando a números de EMERGENCIA:");
  for (int i = 0; i < TOTAL_EMERGENCIA; i++) {
    const char* numero = NUMEROS_EMERGENCIA[i];

    Serial.print("Enviando a ");
    Serial.print(numero);

    bool enviado = modem.sendSMS(numero, mensaje);

    if (enviado) {
      Serial.println(" -> OK");
    } else {
      Serial.println(" -> FALLÓ (Saldo o cobertura).");
    }
    delay(500); // Pausa entre envíos
  }

  Serial.println("--- FIN SMS ---\n");
}

/************************************************************
 *  ENVÍO A API (EMAIL)
 ************************************************************/
void enviarDatosAPI(String tipoAlerta, float distancia, String mensaje) {
  if (!wifi_conectado) return;

  HTTPClient http;
  http.begin(API_BASE_URL);
  http.addHeader("Content-Type", "application/json");

  DynamicJsonDocument doc(1024);
  doc["tipo"] = tipoAlerta;
  doc["mensaje"] = mensaje;
  doc["distanciaDetectada"] = distancia;
  doc["ubicacion"] = UBICACION;
  doc["dispositivoId"] = DISPOSITIVO_ID;

  String payload;
  serializeJson(doc, payload);

  int code = http.POST(payload);
  Serial.println("API Response: " + String(code));

  http.end();
}

/************************************************************
 *  SENSOR
 ************************************************************/
float medirDistancia() {
  digitalWrite(PIN_TRIG, LOW);
  delayMicroseconds(2);
  digitalWrite(PIN_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(PIN_TRIG, LOW);

  long duracion = pulseIn(PIN_ECHO, HIGH, TIMEOUT_SENSOR);
  if (duracion == 0) return -1;

  float distancia = duracion * 0.034 / 2;
  if (distancia < DISTANCIA_MINIMA || distancia > DISTANCIA_MAXIMA) return -1;

  return distancia;
}

/************************************************************
 *  PROCESO DE ALERTAS
 ************************************************************/
void procesarLectura(float distancia) {
  Serial.print("Distancia: ");
  Serial.print(distancia, 1);
  Serial.println(" cm");

  lecturas[indice_lectura] = distancia;
  indice_lectura = (indice_lectura + 1) % NUMERO_LECTURAS;

  float promedio = calcularPromedio();
  ultimo_promedio = promedio;

  Serial.print("Promedio: ");
  Serial.print(promedio, 1);
  Serial.println(" cm");

  if (promedio > 0 && promedio <= DISTANCIA_ALERTA) {
    contador_alarma++;

    if (contador_alarma >= LECTURAS_CONSECUTIVAS && !alarma_activa)
      activarAlarma();

  } else {
    if (contador_alarma > 0) contador_alarma--;
    if (contador_alarma == 0 && alarma_activa)
      desactivarAlarma();
  }
}

float calcularPromedio() {
  float suma = 0;
  int validas = 0;

  for (int i = 0; i < NUMERO_LECTURAS; i++) {
    if (lecturas[i] > 0) {
      suma += lecturas[i];
      validas++;
    }
  }
  return validas == 0 ? -1 : suma / validas;
}

/************************************************************
 *  ACTIVAR / DESACTIVAR ALARMA
 ************************************************************/
void activarAlarma() {
  alarma_activa = true;
  digitalWrite(PIN_LED, HIGH);

  Serial.println("\n>>> ALERTA DE INUNDACIÓN <<<");

  String mensaje = "ALERTA ROJA: Nivel critico a " + String(ultima_distancia, 1) + " cm.";
  enviarSMS(mensaje);
  enviarDatosAPI("ALERTA_ROJA", ultima_distancia, mensaje);
}

void desactivarAlarma() {
  alarma_activa = false;
  digitalWrite(PIN_LED, LOW);

  Serial.println("\n>>> SITUACIÓN NORMALIZADA <<<");

  String mensaje = "AVISO: Nivel de agua normalizado.";
  enviarSMS(mensaje);
  enviarDatosAPI("SITUACION_NORMALIZADA", ultima_distancia, mensaje);
}