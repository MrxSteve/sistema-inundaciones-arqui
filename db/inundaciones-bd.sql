CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    google_id VARCHAR(100) UNIQUE,
    avatar_url VARCHAR(500),
    activo BOOLEAN DEFAULT true,
    tipo_notificacion ENUM('SMS', 'EMAIL', 'AMBOS') DEFAULT 'EMAIL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE alertas (
    id BIGSERIAL PRIMARY KEY,
    distancia_cm DECIMAL(5,2) NOT NULL,
    tipo_alerta ENUM('RIESGO', 'EMERGENCIA', 'NORMALIZADO') NOT NULL,
    mensaje TEXT NOT NULL,
    usuarios_notificados INTEGER DEFAULT 0,
    emails_enviados INTEGER DEFAULT 0,
    sms_enviados INTEGER DEFAULT 0,
    costo_total DECIMAL(10,2) DEFAULT 0.00,
    procesada BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
