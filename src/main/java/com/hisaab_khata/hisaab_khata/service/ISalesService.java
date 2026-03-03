package com.hisaab_khata.hisaab_khata.service;

import com.hisaab_khata.hisaab_khata.dto.sales.PostedSaleResponse;
import com.hisaab_khata.hisaab_khata.dto.sales.SalesDraftRequest;
import com.hisaab_khata.hisaab_khata.dto.sales.SalesDraftResponse;

import java.time.LocalDate;
import java.util.List;

public interface ISalesService {

    SalesDraftResponse createDraft(SalesDraftRequest request);

    SalesDraftResponse getById(Long id);

    PostedSaleResponse post(Long id);

    List<SalesDraftResponse> list(LocalDate from, LocalDate to);
}
