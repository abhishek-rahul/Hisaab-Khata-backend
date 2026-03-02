package com.hisaab_khata.hisaab_khata.service;

import com.hisaab_khata.hisaab_khata.dto.purchaseupload.ParsedInvoiceResponse;

public interface IPurchaseUploadService {

    ParsedInvoiceResponse upload(byte[] fileBytes, String originalFileName, Long supplierPartyId);

    ParsedInvoiceResponse getByUploadId(Long uploadId);
}
