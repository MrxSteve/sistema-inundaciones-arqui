#define TINY_GSM_MODEM_SIM800
#include <TinyGsmClient.h>

// --- 1. DESTINATARIOS DE SMS ---
const char* NUMEROS_DESTINO[] = {
  "+50311112222", // Número 1
  "+50333334444", // Número 2 (Opcional)
  "+50355556666"  // Número 3 (Opcional)
};
const int TOTAL_DESTINOS = sizeof(NUMEROS_DESTINO) / sizeof(NUMEROS_DESTINO[0]);

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

// Declarando las variables globales
float lecturas[NUMERO_LECTURAS];
int indice_lectura = 0;
int contador_alarma = 0;
bool alarma_activa = false;
float ultima_distancia = 0;
float ultimo_promedio = 0;
unsigned long ultima_lectura = 0;

// Declarando funciones
void inicializarModem();
void enviarSMS(String mensaje);
float medirDistancia();
void procesarLectura(float distancia);
float calcularPromedio();
void activarAlarma();
void desactivarAlarma();

void setup() {
  Serial.begin(115200);
  delay(1000);

  pinMode(PIN_TRIG, OUTPUT);
  pinMode(PIN_ECHO, INPUT);
  pinMode(PIN_LED, OUTPUT);
  digitalWrite(PIN_LED, LOW);

  Serial.println("Sistema de alarma de inundaciones (Modo SMS) iniciado");
  
  // Inicializar array de lecturas
  for (int i = 0; i < NUMERO_LECTURAS; i++) {
    lecturas[i] = -1;
  }

  inicializarModem();
  
  Serial.println("\nSistema iniciado y vigilando\n");
}

void loop() {
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
    return;
  }
  Serial.println(" Red detectada.");
}

// Función que recorre la lista de números y envía el SMS
void enviarSMS(String mensaje) {
  Serial.println("--- Iniciando envío de SMS a múltiples números ---");
  for (int i = 0; i < TOTAL_DESTINOS; i++) {
    const char* numero = NUMEROS_DESTINO[i];
    
    Serial.print("Enviando a ");
    Serial.print(numero);
    
    bool enviado = modem.sendSMS(numero, mensaje);
    
    if (enviado) {
      Serial.println(" -> OK");
    } else {
      Serial.println(" -> FALLÓ (Saldo o cobertura).");
    }
    delay(500); // Pequeña pausa entre envíos
  }
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

// Función para calcular el promedio (sin cambios)
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

// Función para activar alarma (envía SMS a todos)
void activarAlarma() {
  alarma_activa = true;
  digitalWrite(PIN_LED, HIGH);
  Serial.println("\n>>> ¡ALERTA DE INUNDACIÓN! <<<");
  
  String msj = "ALERTA ROJA: Riesgo de inundacion. Nivel critico a " + String(ultima_distancia, 1) + " cm.";
  enviarSMS(msj);
}

// Función para desactivar alarma (envía SMS a todos)
void desactivarAlarma() {
  alarma_activa = false;
  digitalWrite(PIN_LED, LOW);
  Serial.println("\n>>> Situación normalizada <<<");
  
  String msj = "AVISO: El nivel de agua ha bajado. Situacion normalizada.";
  enviarSMS(msj);
}