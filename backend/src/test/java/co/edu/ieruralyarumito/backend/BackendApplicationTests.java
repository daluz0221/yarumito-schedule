package co.edu.ieruralyarumito.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

// Prueba de integración que verifica que el contexto de Spring Boot cargue correctamente.
@Testcontainers
@SpringBootTest
class BackendApplicationTests {

    // PostgreSQL real utilizado únicamente durante las pruebas.
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16");

    @Test
    void contextLoads() {
    }
}