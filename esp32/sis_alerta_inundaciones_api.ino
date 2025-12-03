#define TINY_GSM_MODEM_SIM800
#include <TinyGsmClient.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

// --- 1. CONFIGURACIÓN WIFI ---
const char* WIFI_SSID = "TU_WIFI_SSID";
const char* WIFI_PASSWORD = "TU_WIFI_PASSWORD";

// --- 2. CONFIGURACIÓN API ---
const char* API_BASE_URL = "http://192.168.1.122:8085/api/alertas/esp32";
const char* API_NUMEROS_URL = "http://192.168.1.122:8085/api/esp32/numeros-sms";

// --- 3. SISTEMA HÍBRIDO DE NÚMEROS ---
// Números de emergencia críticos (RESPALDO cuando no hay WiFi)
const char* NUMEROS_EMERGENCIA[] = {
  "+50311112222", // Bomberos/Emergencias
  "+50333334444"  // Alcalde/Autoridades
};
const int TOTAL_EMERGENCIA = 2;

// Números dinámicos obtenidos de la API (cuando hay WiFi)
String numerosTelefono[20]; // Array dinámico para hasta 20 números
int totalNumeros = 0;       // Contador de números obtenidos

// Control de cache y respaldo
bool numerosActualizados = false;  // Si se obtuvieron números de la API
unsigned long ultimaActualizacion = 0; // Timestamp de última actualización
const unsigned long TIEMPO_CACHE = 3600000; // 1 hora en millisegundos

// Configuración del puerto serie para el SIM800
HardwareSerial SerialGSM(1);
TinyGsm modem(SerialGSM);

// Definiendo pines a utilizar del ESP32
#define SIM800_RX 16
#define SIM800_TX 17
#define PIN_TRIG 26
#define PIN_ECHO 27
#define PIN_LED 33

// Definiendo limites y tiempos de respuestas
#define DISTANCIA_ALERTA 20
#define DISTANCIA_MAXIMA 400
#define DISTANCIA_MINIMA 2
#define NUMERO_LECTURAS 5
#define LECTURAS_CONSECUTIVAS 3
#define TIMEOUT_SENSOR 30000
#define INTERVALO_LECTURA 500

// ID único del dispositivo ESP32
const String DISPOSITIVO_ID = "ESP32-SENSOR-001";
const String UBICACION = "Zona Norte - Río Principal";

// Declarando las variables globales
float lecturas[NUMERO_LECTURAS];
int indice_lectura = 0;
int contador_alarma = 0;
bool alarma_activa = false;
float ultima_distancia = 0;
float ultimo_promedio = 0;
unsigned long ultima_lectura = 0;

// Estado de conectividad
bool wifi_conectado = false;
bool gsm_conectado = false;

// Declarando funciones
void inicializarWiFi();
void inicializarModem();
bool obtenerNumerosDesdeLaAPI();
void enviarSMS(String mensaje);
void enviarDatosAPI(String tipoAlerta, float distancia, String mensaje);
float medirDistancia();
void procesarLectura(float distancia);
float calcularPromedio();
void activarAlarma();
void desactivarAlarma();
String determinarTipoAlerta(float distancia, bool esAlarma);

void setup() {
  Serial.begin(115200);
  delay(1000);

  pinMode(PIN_TRIG, OUTPUT);
  pinMode(PIN_ECHO, INPUT);
  pinMode(PIN_LED, OUTPUT);
  digitalWrite(PIN_LED, LOW);

  Serial.println("Sistema de alarma de inundaciones (SMS + API) iniciado");

  // Inicializar array de lecturas
  for (int i = 0; i < NUMERO_LECTURAS; i++) {
    lecturas[i] = -1;
  }

  // Inicializar conectividad
  inicializarWiFi();
  inicializarModem();

  // Obtener números de teléfono desde la API
  if (obtenerNumerosDesdeLaAPI()) {
    Serial.println("Números de teléfono obtenidos exitosamente:");
    for (int i = 0; i < totalNumeros; i++) {
      Serial.println(" - " + numerosTelefono[i]);
    }
  } else {
    Serial.println("Error al obtener números de teléfono.");
  }

  Serial.println("\nSistema iniciado y vigilando\n");
  Serial.println("WiFi: " + String(wifi_conectado ? "Conectado" : "Desconectado"));
  Serial.println("GSM: " + String(gsm_conectado ? "Conectado" : "Desconectado"));
}

void loop() {
  // Verificar conectividad WiFi
  if (WiFi.status() != WL_CONNECTED && wifi_conectado) {
    wifi_conectado = false;
    Serial.println("WiFi desconectado, reintentando...");
    inicializarWiFi();
  }

  // Lógica del sensor ultrasónico
  unsigned long t = millis();
  if (t - ultima_lectura >= INTERVALO_LECTURA) {
    ultima_lectura = t;
    float distancia = medirDistancia();

    if (distancia > 0) {
      procesarLectura(distancia);
      ultima_distancia = distancia;
    } else {
      Serial.println("Lectura inválida o fuera de rango");
    }
  }
}

// Función para inicializar WiFi
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
    Serial.println();
    Serial.println("WiFi conectado!");
    Serial.print("IP: ");
    Serial.println(WiFi.localIP());
  } else {
    wifi_conectado = false;
    Serial.println();
    Serial.println("Error: No se pudo conectar a WiFi");
  }
}

// Función para inicializar y esperar la red GSM
void inicializarModem() {
  Serial.println("\nIniciando configuración del módem SIM800...");

  // Iniciar puerto serial del GSM
  SerialGSM.begin(9600, SERIAL_8N1, SIM800_RX, SIM800_TX);
  delay(3000);

  Serial.println("Reiniciando módem...");
  modem.restart();
  String modemInfo = modem.getModemInfo();
  Serial.print("Info del Modem: ");
  Serial.println(modemInfo);

  Serial.print("Esperando red GSM (2G)...");
  if (!modem.waitForNetwork()) {
    Serial.println(" fallo. No se detectó red GSM.");
    gsm_conectado = false;
    return;
  }
  Serial.println(" Red detectada.");
  gsm_conectado = true;
}

// Función que obtiene los números de teléfono desde la API
bool obtenerNumerosDesdeLaAPI() {
  if (!wifi_conectado) {
    Serial.println("WiFi no disponible, no se pueden obtener los números");
    return false;
  }

  Serial.println("--- Obteniendo números de teléfono desde la API ---");

  HTTPClient http;
  http.begin(API_NUMEROS_URL);
  http.addHeader("Content-Type", "application/json");

  int httpResponseCode = http.GET();

  if (httpResponseCode > 0) {
    String response = http.getString();
    Serial.println("API Response Code: " + String(httpResponseCode));
    Serial.println("API Response: " + response);

    if (httpResponseCode == 200) {
      Serial.println("✅ Números obtenidos exitosamente");

      // Parsear respuesta JSON
      DynamicJsonDocument doc(1024);
      deserializeJson(doc, response);

      // Limpiar array de números
      totalNumeros = 0;

      // Recorrer los números en el JSON y agregarlos al array
      for (JsonVariant numero : doc["numeros"]) {
        if (totalNumeros < 20) { // Asegurarse de no exceder el tamaño del array
          numerosTelefono[totalNumeros] = numero.as<String>();
          totalNumeros++;
        }
      }
      numerosActualizados = true;
      ultimaActualizacion = millis();
      return true;
    } else {
      Serial.println("❌ Error al obtener números: " + String(httpResponseCode));
    }
  } else {
    Serial.println("❌ Error de conexión con API: " + String(httpResponseCode));
  }

  http.end();
  return false;
}

// Función que recorre la lista de números y envía el SMS
void enviarSMS(String mensaje) {
  if (!gsm_conectado) {
    Serial.println("GSM no disponible, saltando envío de SMS");
    return;
  }

  Serial.println("--- Iniciando envío de SMS a múltiples números ---");
  for (int i = 0; i < totalNumeros; i++) {
    const char* numero = numerosTelefono[i].c_str();

    Serial.print("Enviando SMS a ");
    Serial.print(numero);

    bool enviado = modem.sendSMS(numero, mensaje);

    if (enviado) {
      Serial.println(" -> OK");
    } else {
      Serial.println(" -> FALLÓ (Saldo o cobertura).");
    }
    delay(500); // Pequeña pausa entre envíos
  }

  // Enviar a números de emergencia si no se actualizaron números recientemente
  if (!numerosActualizados) {
    Serial.println("--- Enviando SMS a números de emergencia ---");
    for (int i = 0; i < TOTAL_EMERGENCIA; i++) {
      const char* numeroEmergencia = NUMEROS_EMERGENCIA[i];

      Serial.print("Enviando SMS a emergencia ");
      Serial.print(numeroEmergencia);

      bool enviado = modem.sendSMS(numeroEmergencia, mensaje);

      if (enviado) {
        Serial.println(" -> OK");
      } else {
        Serial.println(" -> FALLÓ (Saldo o cobertura).");
      }
      delay(500); // Pequeña pausa entre envíos
    }
  }
}

// Función para enviar datos a la API REST
void enviarDatosAPI(String tipoAlerta, float distancia, String mensaje) {
  if (!wifi_conectado) {
    Serial.println("WiFi no disponible, saltando envío a API");
    return;
  }

  Serial.println("--- Enviando datos a API REST ---");

  HTTPClient http;
  http.begin(API_BASE_URL);
  http.addHeader("Content-Type", "application/json");

  // Crear JSON con los datos
  DynamicJsonDocument doc(1024);
  doc["tipo"] = tipoAlerta;
  doc["mensaje"] = mensaje;
  doc["distanciaDetectada"] = distancia;
  doc["ubicacion"] = UBICACION;
  doc["dispositivoId"] = DISPOSITIVO_ID;

  String jsonString;
  serializeJson(doc, jsonString);

  Serial.println("Enviando JSON: " + jsonString);

  int httpResponseCode = http.POST(jsonString);

  if (httpResponseCode > 0) {
    String response = http.getString();
    Serial.println("API Response Code: " + String(httpResponseCode));
    Serial.println("API Response: " + response);

    if (httpResponseCode == 200) {
      Serial.println("✅ Datos enviados exitosamente a la API");

      // Parear respuesta para obtener info
      DynamicJsonDocument responseDoc(1024);
      deserializeJson(responseDoc, response);

      if (responseDoc.containsKey("id")) {
        Serial.println("Alerta creada con ID: " + String((int)responseDoc["id"]));
      }
      if (responseDoc.containsKey("emailsEnviados")) {
        Serial.println("Emails enviados: " + String((int)responseDoc["emailsEnviados"]));
      }
    } else {
      Serial.println("❌ Error en la API: " + String(httpResponseCode));
    }
  } else {
    Serial.println("❌ Error de conexión con API: " + String(httpResponseCode));
  }

  http.end();
}

// Función que obtiene la distancia
float medirDistancia() {
  digitalWrite(PIN_TRIG, LOW);
  delayMicroseconds(2);
  digitalWrite(PIN_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(PIN_TRIG, LOW);

  long duracion = pulseIn(PIN_ECHO, HIGH, TIMEOUT_SENSOR);
  if (duracion == 0) {
    return -1;
  }
  float distancia = duracion * 0.034 / 2;
  if (distancia < DISTANCIA_MINIMA || distancia > DISTANCIA_MAXIMA) {
    return -1;
  }
  return distancia;
}

// Función que sirve para procesar las lecturas
void procesarLectura(float distancia) {
  // Lógica de promedio, idéntica a la versión anterior para estabilidad
  lecturas[indice_lectura] = distancia;
  indice_lectura = (indice_lectura + 1) % NUMERO_LECTURAS;
  float promedio = calcularPromedio();

  if (promedio > 0) {
    ultimo_promedio = promedio;
    Serial.print("Promedio: ");
    Serial.print(promedio, 1);
    Serial.println(" cm");
  }

  if (promedio > 0 && promedio <= DISTANCIA_ALERTA) {
    contador_alarma++;

    if (contador_alarma >= LECTURAS_CONSECUTIVAS && !alarma_activa) {
      activarAlarma();
    }
  } else {
    if (contador_alarma > 0) {
      contador_alarma--;
    }
    if (contador_alarma == 0 && alarma_activa) {
      desactivarAlarma();
    }
  }
}

// Función para calcular el promedio
float calcularPromedio() {
  float suma = 0;
  int validas = 0;

  for (int i = 0; i < NUMERO_LECTURAS; i++) {
    if (lecturas[i] > 0) {
      suma += lecturas[i];
      validas++;
    }
  }
  return (validas == 0) ? -1 : (suma / validas);
}

// Función para activar alarma (envía SMS + API)
void activarAlarma() {
  alarma_activa = true;
  digitalWrite(PIN_LED, HIGH);
  Serial.println("\n>>> ¡ALERTA DE INUNDACIÓN! <<<");

  String tipoAlerta = determinarTipoAlerta(ultima_distancia, true);
  String msj = "ALERTA ROJA: Riesgo de inundacion. Nivel critico a " + String(ultima_distancia, 1) + " cm.";

  // Enviar SMS (método original)
  enviarSMS(msj);

  // Enviar datos a API (nuevo)
  enviarDatosAPI(tipoAlerta, ultima_distancia, msj);
}

// Función para desactivar alarma (envía SMS + API)
void desactivarAlarma() {
  alarma_activa = false;
  digitalWrite(PIN_LED, LOW);
  Serial.println("\n>>> Situación normalizada <<<");

  String tipoAlerta = determinarTipoAlerta(ultima_distancia, false);
  String msj = "AVISO: El nivel de agua ha bajado. Situacion normalizada.";

  // Enviar SMS (método original)
  enviarSMS(msj);

  // Enviar datos a API (nuevo)
  enviarDatosAPI(tipoAlerta, ultima_distancia, msj);
}

// Función para determinar el tipo de alerta para la API
String determinarTipoAlerta(float distancia, bool esAlarma) {
  if (!esAlarma) {
    return "SITUACION_NORMALIZADA";
  }

  if (distancia <= 20) {
    return "ALERTA_ROJA";
  } else if (distancia <= 35) {
    return "ALERTA_AMARILLA";
  }

  return "ALERTA_AMARILLA"; // Por defecto
}
