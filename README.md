# API Bancaria

Sistema bancario REST API para gestionar clientes, cuentas y transacciones.

##  Inicio Rápido

### Requisitos
- Docker
- (Opcional) Java 17+ y Maven, solo si deseas ejecutar la app localmente sin Docker

### Instalación

#### Opción A: Todo con Docker (recomendado)
```bash
# Construir y levantar base de datos, Adminer y la API
docker compose up -d --build

# (Opcional) Ver logs de la API
docker logs -f banca-app

# Abrir en navegador
start http://localhost:8000/swagger-ui/index.html
```

Para detener los servicios:
```bash
docker compose down
```

Nota: el primer build puede tardar unos minutos porque descarga dependencias y crea la imagen.

#### Opción B: Solo BD en Docker y app local (desarrollo)
```bash
# 1) Levantar solo la base de datos y Adminer
docker compose up -d postgres adminer

# 2) Ejecutar la aplicación localmente
mvn spring-boot:run

# 3) Abrir en navegador
start http://localhost:8000/swagger-ui/index.html
```

##  URLs Importantes

| Servicio | URL |
|----------|-----|
| API | http://localhost:8000 |
| Swagger UI | http://localhost:8000/swagger-ui/index.html |
| OpenAPI (JSON) | http://localhost:8000/v3/api-docs |
| Tester Web | http://localhost:8000/lab.html |
| Base de Datos | http://localhost:8081 |

### Documentación con Swagger/OpenAPI
- Swagger UI: [http://localhost:8000/swagger-ui/index.html#/](http://localhost:8000/swagger-ui/index.html#/)
- OpenAPI (JSON): [http://localhost:8000/v3/api-docs](http://localhost:8000/v3/api-docs)

##  Endpoints

### Clientes
- `POST /api/clientes` - Crear cliente
- `GET /api/clientes` - Listar clientes
- `GET /api/clientes/{id}` - Ver cliente
- `PUT /api/clientes/{id}` - Actualizar cliente
- `DELETE /api/clientes/{id}` - Eliminar cliente

### Cuentas
- `POST /api/cuentas` - Crear cuenta
- `POST /api/cuentas/con-saldo` - Crear cuenta con saldo inicial
- `GET /api/cuentas` - Listar cuentas
- `GET /api/cuentas/{id}` - Ver cuenta
- `PUT /api/cuentas/{id}` - Cambiar estado (ACTIVA/INACTIVA/CANCELADA)

### Transacciones
- `POST /api/transacciones/consignacion` - Depositar dinero
- `POST /api/transacciones/retiro` - Retirar dinero
- `POST /api/transacciones/transferencia` - Transferir entre cuentas
- `GET /api/transacciones` - Ver historial (filtros: cuenta, fechaDesde, fechaHasta)

---

## 📦 JSON por método (request/response)

### Clientes
- POST `/api/clientes`
  - Request
  ```json
  {
    "tipoIdentificacion": "CC",
    "numIdentificacion": "1001",
    "nombres": "Juan",
    "apellidos": "Pérez",
    "email": "juan@example.com",
    "fechaNacimiento": "1990-05-10"
  }
  ```
  - Response (201)
  ```json
  {
    "id": "<UUID>",
    "tipoIdentificacion": "CC",
    "numIdentificacion": "1001",
    "nombres": "Juan",
    "apellidos": "Pérez",
    "email": "juan@example.com",
    "fechaNacimiento": "1990-05-10",
    "fechaCreacion": "2025-08-10T12:00:00Z",
    "fechaModificacion": "2025-08-10T12:00:00Z",
    "version": 0
  }
  ```
- GET `/api/clientes`
  - Query params (opcionales): `tipoIdentificacion`, `numIdentificacion`, `nombre`, `apellido`, `email`
  - Response (200)
  ```json
  [
    {
      "id": "<UUID>",
      "tipoIdentificacion": "CC",
      "numIdentificacion": "1001",
      "nombres": "Juan",
      "apellidos": "Pérez",
      "email": "juan@example.com",
      "fechaNacimiento": "1990-05-10",
      "fechaCreacion": "2025-08-10T12:00:00Z",
      "fechaModificacion": "2025-08-10T12:00:00Z",
      "version": 0
    }
  ]
  ```
- GET `/api/clientes/{id}`
  - Response (200): igual a POST (201)
- PUT `/api/clientes/{id}`
  - Request: igual a POST
  - Response (200): cliente actualizado
- DELETE `/api/clientes/{id}`
  - Response (204 No Content)

### Cuentas
- POST `/api/cuentas`
  - Request
  ```json
  {
    "clienteId": "<UUID_CLIENTE>",
    "tipoCuenta": "AHORROS",
    "exentaGmf": false,
    "usuarioPropietario": "empleado.demo"
  }
  ```
  - Response (201)
  ```json
  {
    "id": "<UUID>",
    "clienteId": "<UUID_CLIENTE>",
    "tipoCuenta": "AHORROS",
    "numeroCuenta": "53XXXXXXXX",
    "estado": "ACTIVA",
    "saldo": 0.0,
    "exentaGmf": false,
    "usuarioPropietario": "empleado.demo",
    "fechaCreacion": "2025-08-10T12:00:00Z",
    "fechaModificacion": "2025-08-10T12:00:00Z",
    "version": 0
  }
  ```
- POST `/api/cuentas/con-saldo`
  - Request
  ```json
  {
    "clienteId": "<UUID_CLIENTE>",
    "tipoCuenta": "AHORROS",
    "saldoInicial": 100000.00,
    "exentaGmf": false,
    "usuarioPropietario": "empleado.demo"
  }
  ```
  - Response (201): igual a POST `/api/cuentas` con `saldo` inicial
- GET `/api/cuentas`
  - Query params (opcionales): `clienteId`, `tipoCuenta`, `estado`, `numeroCuenta`
  - Response (200)
  ```json
  [
    {
      "id": "<UUID>",
      "clienteId": "<UUID_CLIENTE>",
      "tipoCuenta": "AHORROS",
      "numeroCuenta": "53XXXXXXXX",
      "estado": "ACTIVA",
      "saldo": 0.0,
      "exentaGmf": false,
      "usuarioPropietario": "empleado.demo",
      "fechaCreacion": "2025-08-10T12:00:00Z",
      "fechaModificacion": "2025-08-10T12:00:00Z",
      "version": 0
    }
  ]
  ```
- GET `/api/cuentas/{id}`
  - Response (200)
  ```json
  {
    "id": "<UUID>",
    "clienteId": "<UUID_CLIENTE>",
    "tipoCuenta": "AHORROS",
    "numeroCuenta": "53XXXXXXXX",
    "estado": "ACTIVA",
    "saldo": 0.0,
    "exentaGmf": false,
    "usuarioPropietario": "empleado.demo",
    "fechaCreacion": "2025-08-10T12:00:00Z",
    "fechaModificacion": "2025-08-10T12:00:00Z",
    "version": 0,
    "movimientosRecientes": [
      {
        "id": "<UUID>",
        "cuentaId": "<UUID>",
        "tipoMov": "CREDIT",
        "monto": 50000.0,
        "saldoAntes": 0.0,
        "saldoDespues": 50000.0,
        "fecha": "2025-08-10T12:00:00Z"
      }
    ]
  }
  ```
- PUT `/api/cuentas/{id}`
  - Request
  ```json
  { "estado": "INACTIVA" }
  ```
  - Response (200): cuenta en `CuentaResponse` (igual a creación con estado actualizado)

### Transacciones
- POST `/api/transacciones/consignacion`
  - Headers opcional: `X-User: empleado.demo`
  - Request
  ```json
  { "cuentaDestinoId": "<UUID_CUENTA>", "monto": 50000, "descripcion": "Consignación" }
  ```
  - Response (201)
  ```json
  {
    "id": "<UUID>",
    "tipo": "CONSIGNACION",
    "fecha": "2025-08-10T12:00:00Z",
    "cuentaDestinoId": "<UUID_CUENTA>",
    "monto": 50000.0,
    "descripcion": "Consignación",
    "referencia": null,
    "estado": "OK",
    "creadoPor": "empleado.demo"
  }
  ```
- POST `/api/transacciones/retiro`
  - Request
  ```json
  { "cuentaOrigenId": "<UUID_CUENTA>", "monto": 20000, "descripcion": "Retiro" }
  ```
  - Response (201): `TransaccionResponse` (tipo `RETIRO`)
- POST `/api/transacciones/transferencia`
  - Request
  ```json
  { "cuentaOrigenId": "<UUID_ORIGEN>", "cuentaDestinoId": "<UUID_DESTINO>", "monto": 30000, "descripcion": "Transferencia" }
  ```
  - Response (201): `TransaccionResponse` (tipo `TRANSFERENCIA`)
- GET `/api/transacciones`
  - Query params (opcionales):
    - `cuenta=<UUID>`
    - `fechaDesde=2025-01-01T00:00:00Z`
    - `fechaHasta=2025-12-31T23:59:59Z`
  - Response (200)
  ```json
  [
    {
      "id": "<UUID>",
      "tipo": "CONSIGNACION",
      "fecha": "2025-08-10T12:00:00Z",
      "cuentaDestinoId": "<UUID_CUENTA>",
      "monto": 50000.0,
      "descripcion": "Consignación",
      "estado": "OK",
      "creadoPor": "empleado.demo"
    }
  ]
  ```

---

## 💡 Ejemplos de Uso

### Crear Cliente
```json
POST /api/clientes
{
  "tipoIdentificacion": "CC",
  "numIdentificacion": "1001",
  "nombres": "Juan",
  "apellidos": "Pérez",
  "email": "juan@example.com",
  "fechaNacimiento": "1990-05-10"
}
```

### Crear Cuenta
```json
POST /api/cuentas
{
  "clienteId": "<UUID_DEL_CLIENTE>",
  "tipoCuenta": "AHORROS",
  "exentaGmf": false
}
```

### Consignar
```json
POST /api/transacciones/consignacion
{
  "cuentaDestinoId": "<UUID_CUENTA>",
  "monto": 50000,
  "descripcion": "Consignación inicial"
}
```

### Retirar
```json
POST /api/transacciones/retiro
{
  "cuentaOrigenId": "<UUID_CUENTA>",
  "monto": 20000,
  "descripcion": "Retiro cajero"
}
```

### Transferir
```json
POST /api/transacciones/transferencia
{
  "cuentaOrigenId": "<UUID_ORIGEN>",
  "cuentaDestinoId": "<UUID_DESTINO>",
  "monto": 30000,
  "descripcion": "Pago servicios"
}
```

##  Reglas de Negocio

### Clientes
- Mayor de 18 años
- Email válido
- Nombres y apellidos mínimo 2 caracteres
- No se puede eliminar si tiene cuentas

### Cuentas
- Tipos: AHORROS o CORRIENTE
- Número se genera automático:
  - Ahorros: 53XXXXXXXX
  - Corriente: 33XXXXXXXX
- Cuenta ahorros nunca puede quedar en negativo
- Solo se cancela con saldo en 0

### Transacciones
- Monto debe ser mayor a 0
- Debe haber fondos suficientes
- No se puede transferir a la misma cuenta
- Todas las operaciones se registran en movimientos

##  Pruebas

```bash
# Ejecutar todas las pruebas
mvn test

# Ejecutar prueba específica
mvn -Dtest=TransaccionServiceTest test

# Ver cobertura
mvn clean test jacoco:report
```

### Cobertura de Tests
- Services: `ClienteServiceTest`, `CuentaServiceTest`, `TransaccionServiceTest`
- Controllers: `ClienteControllerTest`, `CuentaControllerTest`, `TransaccionControllerTest`

## 🗄️ Base de Datos

### Acceso a Adminer
1. Ir a http://localhost:8081
2. Configurar:
  - Sistema: PostgreSQL
  - Servidor: postgres
  - Usuario: banca
  - Contraseña: banca
  - Base de datos: banca

### Git : https://github.com/ehc32/Prueba-Tecnica.git

**Versión**: 1.0.0  
**Actualizado**: 2025
