# Sistema Bancario XYZ — Microservicios, Seguridad OAuth2, Resiliencia y Eventos

**Asignatura:** Desarrollo Backend III (PBY2203)
**Proyecto:** Arquitectura de microservicios con Spring Cloud, OAuth2, Kafka y Docker

---

## Objetivo del proyecto

Implementar una arquitectura de microservicios distribuida para el sistema bancario XYZ que integre:

- Configuración centralizada con **Spring Cloud Config**.
- Descubrimiento de servicios con **Eureka**.
- Seguridad mediante **OAuth 2.0** (Spring Authorization Server + Resource Server).
- Tolerancia a fallos con **Resilience4j** (Circuit Breaker + Retry + Fallback).
- Comunicación asíncrona con **Apache Kafka** (Producer → Topic → Consumer).
- Contenerización con **Docker** y orquestación con **Docker Compose**.
- Persistencia en **MySQL** con los datos procesados por el proyecto Spring Batch previo.

---

## Arquitectura

```
DOCKER
├── config-server     :8888   Configuración centralizada
├── eureka-server     :8761   Service discovery
├── auth-server       :9000   OAuth2 Authorization Server
├── api-cuentas       :8081   Microservicio principal
└── kafka             :9092   Message broker (KRaft, sin Zookeeper)

HOST LOCAL
└── MySQL             :3306   Base de datos bank_batch
```

Flujo de comunicación interna:

```
api-cuentas → config-server:8888
api-cuentas → eureka-server:8761
api-cuentas → auth-server:9000     (validación de tokens JWT)
api-cuentas → kafka:29092          (listener interno Docker)
api-cuentas → host.docker.internal:3306  (MySQL local)
```

---

## Estructura del código

```
spring-cloud-banco-xyz/
│
├── config-server/
│   ├── src/main/resources/
│   │   ├── application.properties          Puerto 8888, perfil native
│   │   └── config-repo/
│   │       └── api-cuentas.properties      Datasource, Kafka, topic
│   └── Dockerfile
│
├── eureka-server/
│   ├── src/main/resources/application.properties   Puerto 8761
│   └── Dockerfile
│
├── auth-server/
│   ├── src/main/java/com/bancoxyz/auth_server/
│   │   ├── AuthServerApplication.java
│   │   └── AuthorizationServerConfig.java  JWKSource RSA-2048, PasswordEncoder
│   ├── src/main/resources/application.properties   Puerto 9000, cliente OAuth2
│   └── Dockerfile
│
├── api-cuentas/
│   ├── src/main/java/com/bancoxyz/api_cuentas/
│   │   ├── controller/
│   │   │   └── CuentaController.java       GET /api/cuentas, POST evento Kafka
│   │   ├── service/
│   │   │   └── CuentaService.java          CircuitBreaker + Retry + Fallback
│   │   ├── security/
│   │   │   └── SecurityConfig.java         OAuth2 Resource Server
│   │   ├── kafka/
│   │   │   ├── KafkaProducerConfig.java
│   │   │   ├── KafkaConsumerConfig.java
│   │   │   ├── TransaccionEventoProducer.java
│   │   │   ├── TransaccionEventoConsumer.java
│   │   │   ├── TransaccionEventoDTO.java
│   │   │   └── MovimientoDTO.java
│   │   ├── model/
│   │   │   ├── AnnualStatementSummary.java  Tabla annual_statement_summary
│   │   │   └── TransaccionDetalle.java      Tabla annual_statements
│   │   ├── repository/
│   │   │   ├── AnnualStatementSummaryRepository.java
│   │   │   └── TransaccionDetalleRepository.java
│   │   ├── dto/
│   │   │   └── CuentaResumenDTO.java
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java
│   │       ├── CuentaNoEncontradaException.java
│   │       └── ErrorResponse.java
│   ├── src/main/resources/application.properties
│   └── Dockerfile
│
├── docker-compose.yml
└── README.md
```

---

## Requisitos previos

| Herramienta | Versión mínima |
|---|---|
| Java | 17 |
| Maven | 3.9+ (o usar `mvnw` incluido) |
| Docker | 24+ |
| Docker Compose | v2 |
| MySQL | 8.0 (local, con `bank_batch` poblada) |

> **MySQL debe estar corriendo localmente** con la base de datos `bank_batch` y los datos del proyecto Spring Batch previo (tablas `annual_statement_summary` y `annual_statements` pobladas).

---

## Instrucciones para ejecutar el proyecto

### Opción A — Con Docker Compose (recomendado para Semana 8)

Desde la raíz del proyecto:

```bash
docker compose up -d --build
```

Esto levanta todos los componentes en orden automático:
`kafka` → `config-server` → `eureka-server` → `auth-server` → `api-cuentas`

Verificar contenedores activos:

```bash
docker ps
```

Detener:

```bash
docker compose down
```

**Nota:** MySQL **no** se levanta con Docker. Debe estar corriendo localmente en el puerto 3306. `api-cuentas` se conecta al host mediante `host.docker.internal:3306`.

---

### Opción B — Ejecución local (sin Docker)

Levantar cada módulo en este orden, cada uno en su propia terminal:

**1. Config Server**
```bash
cd config-server
./mvnw spring-boot:run
```
Verificar: `http://localhost:8888/api-cuentas/default`

**2. Eureka Server**
```bash
cd eureka-server
./mvnw spring-boot:run
```
Verificar: `http://localhost:8761`

**3. Auth Server**
```bash
cd auth-server
./mvnw spring-boot:run
```
Verificar: `http://localhost:9000/actuator/health`

**4. api-cuentas**
```bash
cd api-cuentas
./mvnw spring-boot:run
```
Verificar en `http://localhost:8761` que `API-CUENTAS` aparece registrado.

---

## Pruebas funcionales con Postman

### Paso 1 — Obtener token OAuth2

```
POST http://localhost:9000/oauth2/token
Authorization: Basic  (usuario: api-cuentas-client  |  contraseña: api-cuentas-secret-2026)
Content-Type: application/x-www-form-urlencoded

Body:
grant_type=client_credentials&scope=cuentas.read
```

Respuesta esperada:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiJ9...",
  "token_type": "Bearer",
  "expires_in": 3599
}
```

---

### Paso 2 — Consultar todas las cuentas

```
GET http://localhost:8081/api/cuentas
Authorization: Bearer <access_token>
```

Respuesta esperada: lista de 20 cuentas de `annual_statement_summary`.

---

### Paso 3 — Consultar una cuenta específica

```
GET http://localhost:8081/api/cuentas/103
Authorization: Bearer <access_token>
```

Devuelve `404` con cuerpo estructurado si la cuenta no existe.

---

### Paso 4 — Publicar evento Kafka

```
POST http://localhost:8081/api/cuentas/103/transacciones/evento
Authorization: Bearer <access_token>
```

Sin body. Respuesta `202 Accepted` con el evento publicado.

En la consola de `api-cuentas` deben aparecer logs del **Producer** y luego del **Consumer**:

```
[Kafka Producer] Publicando evento | tipo=TRANSACCIONES_CUENTA_CONSULTADAS | cuentaId=103
[Kafka Producer] Evento publicado correctamente | topic=transacciones-eventos | partition=0 | offset=0
[Kafka Consumer] Evento recibido
[Kafka Consumer] tipo        = TRANSACCIONES_CUENTA_CONSULTADAS
[Kafka Consumer] cuentaId    = 103
[Kafka Consumer] movimientos = 2
```

---

### Paso 5 — Verificar seguridad (sin token)

```
GET http://localhost:8081/api/cuentas
(sin header Authorization)
```

Respuesta esperada: `401 Unauthorized`.

---

## Seguridad OAuth2

| Parámetro | Valor |
|---|---|
| Grant type | `client_credentials` |
| Client ID | `api-cuentas-client` |
| Client Secret | `api-cuentas-secret-2026` |
| Scope | `cuentas.read` |
| Token endpoint | `POST /oauth2/token` |
| JWK Set | `GET /oauth2/jwks` |
| Algoritmo | RS256 (RSA-2048) |
| Duración token | 1 hora |

---

## Resiliencia

Configurado en `api-cuentas` sobre el método `CuentaService.obtenerTodas()`:

| Mecanismo | Configuración |
|---|---|
| Circuit Breaker | Ventana 5 llamadas, umbral 50%, espera 10s en OPEN |
| Retry | Máximo 3 intentos, espera 2s entre intentos |
| Fallback | Devuelve lista vacía si el circuito está abierto |

---

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 4.1.1 |
| Spring Cloud | 2025.1.3 |
| Spring Authorization Server | 7.1.1 |
| Spring Kafka | 4.1.x (BOM) |
| Apache Kafka | 4.0.0 |
| MySQL | 8.0 |
| Resilience4j | (BOM Spring Cloud) |
| Docker / Docker Compose | 24+ / v2 |
