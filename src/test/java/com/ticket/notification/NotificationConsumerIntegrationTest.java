package com.ticket.notification;

import com.ticket.notification.infrastructure.messaging.RabbitConfig;
import com.ticket.notification.infrastructure.persistense.EmailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Testes de integração do NotificationConsumer.
 *
 * Publica mensagens reais no RabbitMQ (Testcontainer) usando as constantes
 * do RabbitConfig de produção — sem hardcode de nomes de exchange/fila.
 * O efeito colateral esperado é verificado diretamente no Postgres (Testcontainer).
 */
@DisplayName("NotificationConsumer — integração")
class NotificationConsumerIntegrationTest extends AbstractIntegrationTest {

    // --- Dados de input dos eventos ---
    private static final long ORDER_ID   = 42L;
    private static final long ORDER_ID_A = 1L;
    private static final long ORDER_ID_B = 2L;
    private static final long USER_ID    = 7L;
    private static final long EVENT_ID   = 99L;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private EmailRepository emailRepository;

    @BeforeEach
    void limparBanco() {
        emailRepository.deleteAll();
    }

    // ------------------------------------------------------------------
    // Cenário 1 — caminho feliz
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Deve salvar EmailEntity no banco ao receber OrderConfirmedEvent")
    void deveRegistrarEmailAoReceberEvento() {
        var event = new OrderConfirmedEvent(ORDER_ID, USER_ID, EVENT_ID);

        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.ORDER_CONFIRMED_ROUTING_KEY,
            event
        );

        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofMillis(200))
            .untilAsserted(() ->
                assertThat(emailRepository.existsById(ORDER_ID))
                    .as("EmailEntity deve ser persistido após receber o evento")
                    .isTrue()
            );

        var email = emailRepository.findById(ORDER_ID).orElseThrow();
        assertThat(email.getUserId()).isEqualTo(USER_ID);
        assertThat(email.getSentAt()).isNotNull();
    }

    // ------------------------------------------------------------------
    // Cenário 2 — idempotência
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Deve ignorar evento duplicado — idempotência garantida")
    void deveIgnorarEventoDuplicado() {
        var event = new OrderConfirmedEvent(ORDER_ID, USER_ID, EVENT_ID);

        // Primeira entrega
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.ORDER_CONFIRMED_ROUTING_KEY,
            event
        );

        // Aguarda a primeira ser processada antes de simular a reentrega
        await()
            .atMost(Duration.ofSeconds(10))
            .until(() -> emailRepository.existsById(ORDER_ID));

        // Segunda entrega — simula "at-least-once" do broker
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.ORDER_CONFIRMED_ROUTING_KEY,
            event
        );

        await()
            .atMost(Duration.ofSeconds(5))
            .pollDelay(Duration.ofSeconds(2))
            .untilAsserted(() ->
                assertThat(emailRepository.count())
                    .as("Deve existir exatamente 1 EmailEntity mesmo com entrega duplicada")
                    .isEqualTo(1L)
            );
    }

    // ------------------------------------------------------------------
    // Cenário 3 — pedidos distintos
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Deve registrar emails distintos para pedidos diferentes")
    void deveRegistrarEmailsParaPedidosDistintos() {
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.ORDER_CONFIRMED_ROUTING_KEY,
            new OrderConfirmedEvent(ORDER_ID_A, USER_ID, EVENT_ID)
        );
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.ORDER_CONFIRMED_ROUTING_KEY,
            new OrderConfirmedEvent(ORDER_ID_B, USER_ID, EVENT_ID)
        );

        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() ->
                assertThat(emailRepository.count())
                    .as("Dois pedidos distintos devem gerar dois registros de email")
                    .isEqualTo(2L)
            );

        assertThat(emailRepository.existsById(ORDER_ID_A)).isTrue();
        assertThat(emailRepository.existsById(ORDER_ID_B)).isTrue();
    }
}
