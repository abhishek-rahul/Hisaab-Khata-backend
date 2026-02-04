package com.hisaab_khata.hisaab_khata.domain;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "daily_closing")
public class DailyClosing extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    private String closingDate; // YYYY-MM-DD

    private Double totalSales;
    private Double cashCollected;
    private Double upiReceived;
    private Double udharGiven;
    private Double profit;
}
