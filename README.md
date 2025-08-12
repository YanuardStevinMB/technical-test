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
- POST `/api/clients`
  - Request
  ```json
  {
  "identificationType": "CC",
  "identificationNumber": "12345667890",
  "firstName": "María",
  "lastName": "Tovar",
  "email": "maria.gonzalez@example.com",
  "birthDate": "1990-04-15"
  }

  ```
  - Response (201)
  ```json
  {
  "status": true,
  "data": {
    "id": "<UUID>",
    "identificationType": "CC",
    "identificationNumber": "12345667890",
    "firstName": "María",
    "lastName": "Tovar",
    "email": "maria.gonzalez@example.com",
    "birthDate": "1990-04-15",
    "createdAt": "2025-08-12T05:45:30.140897Z",
    "updatedAt": "2025-08-12T05:45:30.140897Z",
    "version": 0
  },
  "message": "Cliente creado exitosamente"
  }
  ```
- GET `/api/clients`
  - Query params (opcionales): `identificationType`, `identificationNumber`, `firstName`, `lastName`, `email`
  - Response (200)
  ```json
  [
    {
  "status": true,
  "data": [
    {
      "id": "<UUID>",
      "identificationType": "CC",
      "identificationNumber": "12345667890",
      "firstName": "María",
      "lastName": "González",
      "email": "maria.gonzalez@example.com",
      "birthDate": "1990-04-15",
      "createdAt": "2025-08-12T05:45:30.140897Z",
      "updatedAt": "2025-08-12T05:45:30.140897Z",
      "version": 0
    }
  ],
  "message": "Listado de clientes"
  }
  ]
  ```
- GET `/api/clients/{id}`
  - Response (200): igual a POST (201)
- PUT `/api/clients/{id}`
  - Request: igual a POST
  - Response (200): cliente actualizado
- DELETE `/api/clients/{id}`
  - Response (204 No Content)

### Cuentas
- POST `/api/accounts`
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
- POST `/api/accounts/with-balance`
  - Request
  ```json
  {
  "clientId": "<UUID_CLIENTE>",
  "accountType": "AHORROS",
  "initialBalance": 500000,
  "gmfExempt": true,
  "ownerUser": "empleado.demo"
  }
  ```
  - Response (201)
  ```json
  {
  "status": true,
  "data": {
    "id": "<UUID>",
    "clientId": "<UUID_CLIENTE>",
    "accountType": "AHORROS",
    "accountNumber": "5327386795",
    "status": "ACTIVE",
    "balance": 500000,
    "gmfExempt": true,
    "ownerUser": "string",
    "createdAt": "2025-08-12T05:05:04.894463Z",
    "updatedAt": "2025-08-12T05:05:04.894463Z",
    "version": 0
  },
  "message": "Cuenta creada exitosamente con saldo inicial"
  }
  ```
  - Response (201): igual a POST `/api/accounts` con `saldo` inicial
- GET `/api/accounts`
  - Query params (opcionales): `clientId`, `accountType`, `status`, `accountNumber`
  - Response (200)
  ```json

  {
  "status": true,
  "data": [
    {
      "id": "<UUID>",
      "clientId": "<UUID_CLIENTE>",
      "accountType": "AHORROS",
      "accountNumber": "3388033480",
      "status": "INACTIVE",
      "balance": 30000,
      "gmfExempt": true,
      "ownerUser": "LuisaMartinez",
      "createdAt": "2025-08-12T03:17:01.952188Z",
      "updatedAt": "2025-08-12T04:52:28.380280Z",
      "version": 2
    }
  ],
  "message": "Listado de cuentas"
  }
  }
  ```
- GET `/api/accounts/{id}`
  - Response (200)
  ```json
  {
  "status": true,
  "data": {
    "id": "<UUID>",
    "clientId": "<UUID_CLIENTE>",
    "accountType": "AHORROS",
    "accountNumber": "53XXXXXXXX",
    "status": "ACTIVE",
    "balance": 30000,
    "gmfExempt": true,
    "ownerUser": "LuisaMartinez",
    "createdAt": "2025-08-12T03:17:01.952188Z",
    "updatedAt": "2025-08-12T04:46:32.607886Z",
    "version": 1,
    "recentMovements": [
      {
        "id": "<UUID>",
        "accountId": "<UUID_CUENTA>",
        "movementType": "CREDIT",
        "amount": 30000,
        "balanceBefore": 0,
        "balanceAfter": 30000,
        "date": "2025-08-12T04:46:32.590561Z"
      }
    ]
  },
  "message": "Cuenta encontrada"
  }

  ```
- PUT `/api/accounts/{id}`
  - Request
  ```json
  { "status": "INACTIVE" }
  ```
  - Response (200 ): cuenta en `CuentaResponse` (igual a creación con estado actualizado)

### Transacciones
- POST `/api/transactions/deposit`
  - Headers opcional: `X-User: empleado.demo`
  - Request
  ```json
  { "destinationAccountId": "<UUID_CUENTA>", "amount": 50000, "description": "Consignación" }
  ```
  - Response (201)
  ```json

  {
  "status": true,
  "data": {
    "id": "<UUID>",
    "type": "CONSIGNACION",
    "date": "2025-08-12T04:17:55.260187300Z",
    "sourceAccountId": null,
    "destinationAccountId": "<UUID_CUENTA>",
    "amount": 5000000,
    "description": "Consignación",
    "reference": null,
    "status": "OK",
    "createdBy": "system"
  },
  "message": "Transacción de consignación creada exitosamente"
}


- POST `/api/transactions/withdraw`
  - Headers : `X-User: empleado.demo`
  - Request
  ```json
  {
  "sourceAccountId": "<UUID_CUENTA>",
  "amount": 20000,
  "description": "retiro"
  }
  
  ```
  - - Response (201): `TransaccionResponse` (tipo `RETIRO`)
  ```json
  {
  "status": true,
  "data": {
    "id": "<UUID>",
    "type": "RETIRO",
    "date": "2025-08-12T04:20:51.083686Z",
    "sourceAccountId": "<UUID_CUENTA>",
    "destinationAccountId": null,
    "amount": 20000,
    "description": "retiro",
    "reference": null,
    "status": "OK",
    "createdBy": "system"
  },
  "message": "Transacción de retiro creada exitosamente"
}

- POST `/api/transactions/transfer`
- Headers : `X-User: empleado.demo`
  
  - Request
  ```json
  {
  "sourceAccountId": "<UUID_ORIGEN>",
  "destinationAccountId": "<UUID_DESTINO>",
  "amount": 20000,
  "description": "Transferencia"
  }
  
  ```
  - Response (201): `TransaccionResponse` (tipo `TRANSFERENCIA`)
  ```json
    {
      "status": true,
      "data": {
        "id": "<UUID>",
        "type": "TRANSFERENCIA",
        "date": "2025-08-12T04:29:50.662934300Z",
        "sourceAccountId": "<UUID_ORIGEN>",
        "destinationAccountId": "<UUID_DESTINO>",
        "amount": 20000,
        "description": "Transferencia",
        "reference": null,
        "status": "OK",
        "createdBy": "empleado.demo"
      },
      "message": "Transacción de transferencia creada exitosamente"
    }

  ```

- GET `/api/transactions`
  - Query params (opcionales):
    - `cuenta=<UUID>`
    - `fechaDesde=2025-01-01T00:00:00Z`
    - `fechaHasta=2025-12-31T23:59:59Z`
  - Response (200)
  ```json
  {
    "status": null,
     "data": [
     {
      "id": "<UUID>",
      "type": "TRANSFERENCIA",
      "date": "2025-08-12T04:29:50.662934Z",
      "sourceAccountId": "<UUID_ORIGEN>",
      "destinationAccountId": "<UUID_DESTINO>",
      "amount": 20000,
      "description": "Préstamo",
      "reference": null,
      "status": "OK",
      "createdBy": "empleado.demo"
     },
     {
      "id": "<UUID>",
      "type": "RETIRO",
      "date": "2025-08-12T04:15:36.499253Z",
      "sourceAccountId": "<UUID_ORIGEN>",
      "destinationAccountId": null,
      "amount": 20000,
      "description": "retiro",
      "reference": null,
      "status": "OK",
      "createdBy": "system"
     },
  ],
  "message": "Listado de transacciones obtenido exitosamente"
  }
  
  ```

### POST `/api/transactions/transfer`

Realiza una transferencia de dinero entre dos cuentas.

- **Headers (opcionales)**
  - `X-User`: Nombre del usuario que realiza la operación (ej. `empleado.demo`).

- **Request Body**

```json
{
  "sourceAccountId": "<UUID_ORIGEN>",
  "destinationAccountId": "<UUID_DESTINO>",
  "amount": 20000,
  "description": "Prestamo"
}
  ```
Response (201 Created) 
```json
  
    {
      "status": null,
      "data": {
        "id": "<UUID>",
        "type": "TRANSFERENCIA",
        "date": "2025-08-12T04:00:22.972655500Z",
        "sourceAccountId": "<UUID_ORIGEN>",
        "destinationAccountId": "<UUID_DESTINO>",
        "amount": 20000,
        "description": "Préstamo",
        "reference": null,
        "status": "OK",
        "createdBy": "empleado.demo"
      },
      "message": "Transacción de transferencia creada exitosamente"
    }
  
  
  ```
---

## 💡 Ejemplos de Uso

### Crear Cliente
```json
POST /api/clients
{
  "identificationType": "CC",
  "identificationNumber": "12345667890",
  "firstName": "Mari",
  "lastName": "Tovar",
  "email": "Mari.Tovar@example.com",
  "birthDate": "1990-04-15"
}

```

### Crear Cuenta
```json
POST /api/accounts

{
  "clientId": "<UUID_DEL_CLIENTE>",
  "accountType": "AHORROS",
  "gmfExempt": true,
  "ownerUser": "Mari"
}
```

### Consignar
```json
POST /api/transactions/deposit

{
  "destinationAccountId": "<UUID_CUENTA>",
  "amount": 40000,
  "description": "Consignación inicial"
}
```

### Retirar
```json
POST /api/transactions/withdraw

{
  "sourceAccountId": "<UUID_CUENTA>",
  "amount": 20000,
  "description": "Retiro cajero"
}
```

### Transferir
```json
POST /api/transactions/transfer

{
  "sourceAccountId": "<UUID_ORIGEN>",
  "destinationAccountId": "<UUID_DESTINO>",
  "amount": 20000,
  "description": "Pago servicios"
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
