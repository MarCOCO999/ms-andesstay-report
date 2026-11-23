# ms-andesstay-report

Microservicio de reportería del proyecto **AndesStay** (caso DSY1107 – Desarrollo Cloud Native I, Duoc UC): consume el mismo stream de eventos de reservas de **Kafka** que `ms-andesstay-audit`, pero para calcular KPIs de negocio en vez de un historial de auditoría.

## Rol en la arquitectura

- Consume el tópico `reservations.events` (publicado por `ms-andesstay-reservations`) con su propio *consumer group* (`ms-andesstay-report`), independiente del de `ms-andesstay-audit` — ambos servicios reciben todos los eventos sin interferirse. Va acumulando los hechos de cada reserva en Oracle para calcular, bajo demanda, los KPIs que expone su API.
- Consume el tópico `audit.timeline` (publicado por `ms-andesstay-audit`, compactado por reserva) con otro grupo (`ms-andesstay-report-snapshots`) para mantener el **estado actual de cada reserva** (`RESERVATION_SNAPSHOTS`) sin consultar la base de reservations. Como el tópico es compactado, el snapshot se puede reconstruir completo reseteando ese grupo al inicio (`ms-andesstay-kafka-admin`).

## Kafka

| Pieza | Clase | Detalle |
|---|---|---|
| Consumidor de eventos | `messaging/ReservationEventListener` | `reservations.events` → `service/ReservationMetricsService` (idempotente por `eventId`). |
| Consumidor del timeline | `messaging/AuditTimelineListener` | `audit.timeline` → `service/ReservationSnapshotService`: descarta entradas más antiguas que el estado ya aplicado; una tombstone (valor `null`) elimina el snapshot. |
| Tópicos | `config/KafkaTopicsConfig` | Declara los DLT a los que escribe: `reservations.events.DLT` y `audit.timeline.DLT`. |
| Errores | `config/KafkaConsumerConfig` | Igual que audit: payload inválido o JSON ilegible directo al DLT; errores transitorios con 3 reintentos exponenciales; todo con log contextual. |
| Monitor DLT | `messaging/DeadLetterMonitor` | Grupo `ms-andesstay-report-dlt-monitor` sobre `audit.timeline.DLT`. |

Cada listener tiene 3 hilos (1 por partición) y su propio grupo: cada flujo confirma sus offsets por separado.
- API exclusiva para el rol `Admin` (el Operador gestiona reservas, pero no tiene acceso a los indicadores agregados del negocio).

## KPIs disponibles

- **Ocupación activa**: unidades actualmente ocupadas (reservas en estado `CONFIRMADA`, `CHECKIN_PENDIENTE` o `EN_ESTADIA`).
- **Tiempo de ciclo promedio**: minutos promedio entre la creación de una reserva y su checkout.
- **Reservas por hora**: distribución horaria de reservas creadas en el rango consultado.
- **Unidades más reservadas** (`top-units`): ranking de unidades por cantidad de reservas en el rango.

## Stack

- Spring Boot 4.1.1 / Java 25 (LTS)
- Spring Data JPA + Oracle Database (driver `ojdbc17`)
- Spring Kafka (consumidor de `reservations.events` y `audit.timeline`, con reintentos y dead-letter topics)
- Spring Security OAuth2 Resource Server (JWT de Azure AD)

## Configuración

| Variable | Descripción | Default |
|---|---|---|
| `SERVER_PORT` | Puerto HTTP | `8085` |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | Conexión a Oracle | `jdbc:oracle:thin:@localhost:1521/FREEPDB1` / `andesstay` / `changeit` |
| `KAFKA_BOOTSTRAP_SERVERS` | Brokers de Kafka | `localhost:9092,localhost:9093,localhost:9094` |
| `KAFKA_TOPIC_REPLICATION_FACTOR` / `KAFKA_MIN_INSYNC_REPLICAS` | Réplicas y `min.insync.replicas` de los tópicos que declara | `3` / `2` (usar `1` / `1` con un Kafka de un solo broker) |
| `AZURE_ISSUER_URI` / `AZURE_API_AUDIENCE` | Validación del JWT de Azure AD | tenant/App ID URI del caso |

## Endpoints principales

| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| `GET` | `/api/report/kpis?range=last24h` | Admin | Ocupación activa, tiempo de ciclo promedio y reservas por hora |
| `GET` | `/api/report/top-units?range=last7d&limit=10` | Admin | Ranking de unidades más reservadas |
| `GET` | `/api/report/status-summary` | Admin | Reservas por estado actual y por rol que hizo el último cambio (desde `audit.timeline`) |

## Cómo ejecutarlo en local

Requiere Java 25, Oracle y Kafka+Zookeeper levantados (ver [andesstay-infra](https://github.com/MarCOCO999/andesstay-infra)).

```bash
./mvnw spring-boot:run
```

## Repos relacionados

[frontend-andesstay](https://github.com/MarCOCO999/frontend-andesstay) · [ms-andesstay-bff](https://github.com/MarCOCO999/ms-andesstay-bff) · [ms-andesstay-reservations](https://github.com/MarCOCO999/ms-andesstay-reservations) · [ms-andesstay-catalog](https://github.com/MarCOCO999/ms-andesstay-catalog) · [ms-andesstay-notify](https://github.com/MarCOCO999/ms-andesstay-notify) · [ms-andesstay-audit](https://github.com/MarCOCO999/ms-andesstay-audit) · [ms-andesstay-rabbit-admin](https://github.com/MarCOCO999/ms-andesstay-rabbit-admin) · [ms-andesstay-kafka-admin](https://github.com/MarCOCO999/ms-andesstay-kafka-admin) · [andesstay-infra](https://github.com/MarCOCO999/andesstay-infra)
