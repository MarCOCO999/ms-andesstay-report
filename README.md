# ms-andesstay-report

Microservicio de reportería del proyecto **AndesStay** (caso DSY1107 – Desarrollo Cloud Native I, Duoc UC): consume el mismo stream de eventos de reservas de **Kafka** que `ms-andesstay-audit`, pero para calcular KPIs de negocio en vez de un historial de auditoría.

## Rol en la arquitectura

- Consume el tópico `reservations.events` (publicado por `ms-andesstay-reservations`) con su propio *consumer group*, independiente del de `ms-andesstay-audit` — ambos servicios leen el mismo tópico sin interferirse.
- Va acumulando los hechos de cada reserva en Oracle para poder calcular, bajo demanda, los KPIs que expone su API.
- API exclusiva para el rol `Admin` (el Operador gestiona reservas, pero no tiene acceso a los indicadores agregados del negocio).

## KPIs disponibles

- **Ocupación activa**: unidades actualmente ocupadas (reservas en estado `CONFIRMADA`, `CHECKIN_PENDIENTE` o `EN_ESTADIA`).
- **Tiempo de ciclo promedio**: minutos promedio entre la creación de una reserva y su checkout.
- **Reservas por hora**: distribución horaria de reservas creadas en el rango consultado.
- **Unidades más reservadas** (`top-units`): ranking de unidades por cantidad de reservas en el rango.

## Stack

- Spring Boot 4.1.1 / Java 21
- Spring Data JPA + Oracle Database (driver `ojdbc17`)
- Spring Kafka (consumidor de `reservations.events`, con manejo de dead-letter igual que `ms-andesstay-audit`)
- Spring Security OAuth2 Resource Server (JWT de Azure AD)

## Configuración

| Variable | Descripción | Default |
|---|---|---|
| `SERVER_PORT` | Puerto HTTP | `8085` |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | Conexión a Oracle | `jdbc:oracle:thin:@localhost:1521/FREEPDB1` / `andesstay` / `changeit` |
| `KAFKA_BOOTSTRAP_SERVERS` | Broker de Kafka | `localhost:9092` |
| `KAFKA_TOPIC_REPLICATION_FACTOR` | Factor de replicación de los tópicos propios | `1` (el caso pide 3 en el clúster de EC2 de evaluación) |
| `AZURE_ISSUER_URI` / `AZURE_API_AUDIENCE` | Validación del JWT de Azure AD | tenant/App ID URI del caso |

## Endpoints principales

| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| `GET` | `/api/report/kpis?range=last24h` | Admin | Ocupación activa, tiempo de ciclo promedio y reservas por hora |
| `GET` | `/api/report/top-units?range=last7d&limit=10` | Admin | Ranking de unidades más reservadas |

## Cómo ejecutarlo en local

Requiere Java 21, Oracle y Kafka+Zookeeper levantados (ver [andesstay-infra](https://github.com/MarCOCO999/andesstay-infra)).

```bash
./mvnw spring-boot:run
```

## Repos relacionados

[frontend-andesstay](https://github.com/MarCOCO999/frontend-andesstay) · [ms-andesstay-bff](https://github.com/MarCOCO999/ms-andesstay-bff) · [ms-andesstay-reservations](https://github.com/MarCOCO999/ms-andesstay-reservations) · [ms-andesstay-catalog](https://github.com/MarCOCO999/ms-andesstay-catalog) · [ms-andesstay-notify](https://github.com/MarCOCO999/ms-andesstay-notify) · [ms-andesstay-audit](https://github.com/MarCOCO999/ms-andesstay-audit) · [andesstay-infra](https://github.com/MarCOCO999/andesstay-infra)
