package com.tpagrupo17.Metamapa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling



/* Definimos como sesiones mixtas a la implementación que realizamos cuando nuestro frontend está implementado con un cliente liviano desacoplado

- Sesiones en cookies desde el usuario final hasta el servidor frontend
- Tokens de autorización desde el servidor frontend hasta el servidor de aplicación (server back, donde reside logica de negocio)

cliente -> cookie -> server front -> token auth -> server back

- Se usan tokens auth porque el server back es stateless. No recuerda al cliente. En cambio, el server front es stateful. Recuerda al cliente por la sesión

*/

public class ServerBackApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerBackApplication.class, args);
    }
}