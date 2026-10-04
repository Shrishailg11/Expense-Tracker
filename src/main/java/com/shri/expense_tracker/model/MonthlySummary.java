package com.shri.expense_tracker.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_summaries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "month"}))
@Getter
@Setter
@NoArgsConstructor
public class MonthlySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String month;

    @Column(nullable = false)
    private BigDecimal totalSpent;

    @Column(nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public MonthlySummary(User user, String month, BigDecimal totalSpent) {
        this.user = user;
        this.month = month;
        this.totalSpent = totalSpent;
    }
}