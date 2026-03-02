package com.hisaab_khata.hisaab_khata.dto.purchaseupload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedInvoiceResponse {
    private Long uploadId;
    private String docKind;
    private String parserKey;
    private List<ParsedLineResponse> lines;
}
