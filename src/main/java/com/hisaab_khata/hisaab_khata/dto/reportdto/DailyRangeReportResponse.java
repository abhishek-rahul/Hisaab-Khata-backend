package com.hisaab_khata.hisaab_khata.dto.reportdto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyRangeReportResponse {

    private LocalDate from;
    private LocalDate to;
    private List<DailyReportResponse> days;
    private BigDecimal totalSalesSum;
    private BigDecimal totalPurchaseSum;
    private BigDecimal cashInSum;
    private BigDecimal cashOutSum;
    private BigDecimal receivableSum;
    private BigDecimal payableSum;
}
