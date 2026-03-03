package com.hisaab_khata.hisaab_khata.dto.sales;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostedSaleResponse {

    private Long id;
    private String status;  // POSTED
    private LocalDateTime postedAt;
}
