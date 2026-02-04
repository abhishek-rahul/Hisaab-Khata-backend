package com.hisaab_khata.hisaab_khata.dto.reportdto;

import lombok.*;

import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodaySummaryResponse {

    private String date;

    private Double cashCollected;
    private Double upiCollected;
    private Double udharGiven;

    private Double todaySales;
    private Double todayProfit;

    private List<LowStockItemResponse> lowStock;
}

