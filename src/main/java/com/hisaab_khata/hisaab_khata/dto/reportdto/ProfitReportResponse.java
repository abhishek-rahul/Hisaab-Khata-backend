package com.hisaab_khata.hisaab_khata.dto.reportdto;

import lombok.*;

import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitReportResponse {

    private Double totalProfit;

    private List<DailyProfitEntry> dailyBreakdown;
}

