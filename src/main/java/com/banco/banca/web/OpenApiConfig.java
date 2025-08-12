package com.banco.banca.web;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "API Bancaria",
                version = "v1",
                description = "APIs para la gestión de clientes, cuentas y transacciones",
                contact = @Contact(name = "Equipo Bancariao"),
                license = @License(name = "MIT")
        ),
        servers = {
                @Server(url = "/", description = "Servidor por defecto")
        }
)
public class OpenApiConfig {
} 