package com.hisaab_khata.hisaab_khata.service;


import com.hisaab_khata.hisaab_khata.dto.purchasedto.PurchaseCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.purchasedto.PurchaseResponse;

import java.util.List;

public interface IPurchaseService {

    PurchaseResponse createPurchase(PurchaseCreateRequest request);

    PurchaseResponse getPurchase(Long id);

    List<PurchaseResponse> getAllPurchases();

    void updatePurchase(Long id, Long supplierId); // only supplier change allowed
}

