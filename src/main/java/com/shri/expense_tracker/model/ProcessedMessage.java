package com.shri.expense_tracker.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_messages")
@Getter
@NoArgsConstructor
public class ProcessedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String messageKey;

    @Column(nullable = false)
    private LocalDateTime processedAt = LocalDateTime.now();

    public ProcessedMessage(String messageKey) {
        this.messageKey = messageKey;
    }
}