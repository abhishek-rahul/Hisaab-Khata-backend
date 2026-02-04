package com.hisaab_khata.hisaab_khata.dto.reportdto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class DailyProfitEntry {
    private String date;
    private Double profit;
}

