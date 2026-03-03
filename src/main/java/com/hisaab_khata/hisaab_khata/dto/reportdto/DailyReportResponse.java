package com.hisaab_khata.hisaab_khata.dto.reportdto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportResponse {

    private LocalDate date;
    private BigDecimal totalSales;
    private BigDecimal totalPurchase;
    private BigDecimal cashIn;
    private BigDecimal cashOut;
    private BigDecimal receivable;
    private BigDecimal payable;
}
