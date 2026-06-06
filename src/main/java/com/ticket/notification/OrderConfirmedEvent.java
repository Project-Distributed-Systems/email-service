package com.ticket.notification;

import java.io.Serializable;

public record OrderConfirmedEvent(
    Long orderId,
    Long userId,
    Long eventId) implements Serializable {
}
