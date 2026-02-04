package com.hisaab_khata.hisaab_khata.dto.reportdto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyClosingResponse {

    private String closingDate;

    private Double totalSales;
    private Double cashCollected;
    private Double upiCollected;
    private Double udharGiven;
    private Double totalProfit;

    private String notes;
}

