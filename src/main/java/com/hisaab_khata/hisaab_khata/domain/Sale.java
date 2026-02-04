package com.hisaab_khata.hisaab_khata.domain;

import com.hisaab_khata.hisaab_khata.enums.PaymentMode;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sale")
public class Sale extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MULTI-TENANCY
    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer; // nullable for walk-in

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "cash_paid")
    private Double cashPaid;

    @Column(name = "upi_paid")
    private Double upiPaid;

    @Column(name = "udhar_amount")
    private Double udharAmount;

    @Column(name = "round_off")
    private Double roundOff;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false)
    private PaymentMode paymentMode;

    // Computed & stored for reporting
    @Column(name = "profit")
    private Double profit;


}

