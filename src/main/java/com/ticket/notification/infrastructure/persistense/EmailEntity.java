package com.ticket.notification.infrastructure.persistense;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sent_emails")
public class EmailEntity {

  @Id
  private Long orderId; // orderId as PK idempotency key

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private LocalDateTime sentAt;

  public EmailEntity() {
  }

  public EmailEntity(Long orderId, Long userId, LocalDateTime sentAt) {
    this.orderId = orderId;
    this.userId = userId;
    this.sentAt = sentAt;
  }

  public Long getOrderId() {
    return orderId;
  }

  public Long getUserId() {
    return userId;
  }

  public LocalDateTime getSentAt() {
    return sentAt;
  }
}
