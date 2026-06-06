package com.ticket.notification.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.ticket.notification.OrderConfirmedEvent;
import com.ticket.notification.infrastructure.persistense.EmailEntity;
import com.ticket.notification.infrastructure.persistense.EmailRepository;

import java.time.LocalDateTime;

@Component
public class NotificationConsumer {

  private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

  private final EmailRepository repository;

  public NotificationConsumer(EmailRepository repository) {
    this.repository = repository;
  }

  @RabbitListener(queues = RabbitConfig.ORDER_CONFIRMED_QUEUE)
  public void handleOrderConfirmed(OrderConfirmedEvent event) {
    log.info("Received order.confirmed for order {}", event.orderId());

    // idempotency check, "at-least-once" = this can arrive twice
    if (repository.existsById(event.orderId())) {
      log.warn("Email already sent for order {}; skipping duplicate", event.orderId());
      return;
    }

    // "send" the email structured log line
    log.info("{\"event\":\"email_sent\",\"to\":\"user-{}\",\"template\":\"ticket_confirmation\",\"order_id\":{}}",
        event.userId(), event.orderId());

    repository.save(new EmailEntity(event.orderId(), event.userId(), LocalDateTime.now()));
  }
}
