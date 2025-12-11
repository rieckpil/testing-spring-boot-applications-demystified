package de.rieckpil.blog;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class LocalDevTestcontainerConfig {

  @Bean
  @ServiceConnection
  static PostgreSQLContainer<?> postgres() {
    return new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withInitScript("init-postgres.sql"); // Initialize PostgreSQL with required extensions
  }
}
