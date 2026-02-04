package com.hisaab_khata.hisaab_khata.dto.reportdto;

import com.hisaab_khata.hisaab_khata.dto.stockdto.StockResponse;
import lombok.*;

import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class StockReportResponse {
    private List<StockResponse> stock;
}

