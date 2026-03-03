package com.hisaab_khata.hisaab_khata.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "daily_summary")
public class DailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "total_sales", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalSales;

    @Column(name = "cash_in", nullable = false, precision = 18, scale = 2)
    private BigDecimal cashIn;

    @Column(name = "receivable", nullable = false, precision = 18, scale = 2)
    private BigDecimal receivable;

    @Column(name = "total_purchase", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalPurchase;

    @Column(name = "cash_out", nullable = false, precision = 18, scale = 2)
    private BigDecimal cashOut;

    @Column(name = "payable", nullable = false, precision = 18, scale = 2)
    private BigDecimal payable;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
