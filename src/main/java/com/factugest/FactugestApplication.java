package com.factugest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Factugest.
 *
 * @SpringBootApplication combina tres anotaciones en una:
 *   - @Configuration: esta clase puede registrar beans en el contenedor de Spring
 *   - @EnableAutoConfiguration: Spring Boot configura automáticamente lo que detecta
 *     en el classpath (datasource, security, MVC, etc.)
 *   - @ComponentScan: escanea recursivamente el paquete com.factugest buscando
 *     @Controller, @Service, @Repository, @Component y los registra como beans
 */
@SpringBootApplication
public class FactugestApplication {

    /**
     * SpringApplication.run arranca el servidor embebido Tomcat, inicializa el
     * contexto de Spring e inyecta todas las dependencias antes de abrir el puerto.
     */
    public static void main(String[] args) {
        SpringApplication.run(FactugestApplication.class, args);
    }
}
