package com.hisaab_khata.hisaab_khata.dto.khatadto;

import lombok.*;

import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class KhataReportResponse {
    private List<PendingKhataResponse> pendingKhata;
}

