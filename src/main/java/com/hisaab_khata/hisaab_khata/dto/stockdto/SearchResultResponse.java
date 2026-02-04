package com.hisaab_khata.hisaab_khata.dto.stockdto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultResponse {
    private String type;  // PRODUCT / CATEGORY
    private Long id;
    private String name;
}

