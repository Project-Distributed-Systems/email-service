package com.ticket.notification;

import com.ticket.notification.infrastructure.messaging.RabbitConfig;
import com.ticket.notification.infrastructure.persistense.EmailRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class NotificationConsumerTest {

  @Autowired
  RabbitTemplate rabbitTemplate;

  @Autowired
  EmailRepository emailRepository;

  @Test
  void shouldSaveEmailWhenOrderConfirmedEventIsReceived() {
    var event = new OrderConfirmedEvent(42L, 7L, 99L);

    rabbitTemplate.convertAndSend(
        RabbitConfig.EXCHANGE,
        RabbitConfig.ORDER_CONFIRMED_ROUTING_KEY,
        event);

    await().atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(emailRepository.existsById(42L)).isTrue());
  }

  @Test
  void shouldIgnoreDuplicateOrderConfirmedEvent() {
    var event = new OrderConfirmedEvent(100L, 7L, 99L);

    rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ORDER_CONFIRMED_ROUTING_KEY, event);
    rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ORDER_CONFIRMED_ROUTING_KEY, event);

    await().atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(emailRepository.existsById(100L)).isTrue());

    assertThat(emailRepository.findAll())
        .filteredOn(e -> e.getOrderId().equals(100L))
        .hasSize(1);
  }
}
