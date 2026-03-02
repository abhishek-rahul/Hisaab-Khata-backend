package com.hisaab_khata.hisaab_khata.dto.purchasedraft;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaveDraftRequest {
    @NotNull(message = "version is required")
    private Integer version;
    private Long supplierPartyId;
    private String invoiceNo;
    private LocalDate invoiceDate;
    private String notes;
    private List<SaveDraftLineRequest> lines;
}
