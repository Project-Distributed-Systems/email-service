package com.ticket.notification;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Classe base para todos os testes de integração do notification-service.
 *
 * Delega a criação e o ciclo de vida dos containers ao
 * TestcontainersConfiguration, que usa @ServiceConnection — o Spring Boot
 * injeta automaticamente as propriedades de conexão (host, porta, usuário,
 * senha) sem precisar de @DynamicPropertySource manual.
 *
 * Os beans do TestcontainersConfiguration são estáticos dentro do
 * @TestConfiguration, então os containers são compartilhados entre todos
 * os testes da suíte — um container por execução, não um por teste.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {
    // Infraestrutura gerenciada pelo TestcontainersConfiguration.
    // Nada a declarar aqui — @ServiceConnection cuida de tudo.
}
