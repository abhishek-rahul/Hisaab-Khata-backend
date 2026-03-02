package com.hisaab_khata.hisaab_khata.dto.purchasedraft;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateDraftRequest {
    @NotNull(message = "uploadId is required")
    private Long uploadId;
    private Long supplierPartyId;    // optional
    private String invoiceNo;       // optional
    private LocalDate invoiceDate;  // optional
    private String notes;           // optional
}
