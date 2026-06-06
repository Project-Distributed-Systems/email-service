package com.ticket.notification;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
// TestcontainersConfiguration.java
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  RabbitMQContainer rabbitMQContainer() {
    return new RabbitMQContainer("rabbitmq:3.13-management-alpine");
  }

  @Bean
  @ServiceConnection
  PostgreSQLContainer postgresContainer() {
    return new PostgreSQLContainer("postgres:16-alpine")

        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
  }
}
