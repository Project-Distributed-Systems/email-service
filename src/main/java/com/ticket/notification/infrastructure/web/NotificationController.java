package com.ticket.notification.infrastructure.web;

import org.springframework.web.bind.annotation.*;

import com.ticket.notification.infrastructure.persistense.EmailEntity;
import com.ticket.notification.infrastructure.persistense.EmailRepository;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

  private final EmailRepository repository;

  public NotificationController(EmailRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<EmailEntity> all() {
    return repository.findAll();
  }
}
