package com.hisaab_khata.hisaab_khata.dto.reportdto;

import lombok.*;


import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportResponse {

    private String fromDate;
    private String toDate;

    private Double totalSales;     // SUM of Sale.totalAmount
    private Double totalProfit;    // SUM of Sale.profit
    private Long totalBills;           // Count of sale records

    private Double cashCollected;  // SUM(sale.cashPaid)
    private Double upiCollected;   // SUM(sale.upiPaid)
    private Double udharGiven;     // SUM(sale.udharAmount)

    private List<DailyProfitEntry> dailyBreakdown;
}


