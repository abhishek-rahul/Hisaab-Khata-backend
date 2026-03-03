package com.hisaab_khata.hisaab_khata.service;

import com.hisaab_khata.hisaab_khata.dto.purchasedraft.PostedInvoiceSummary;

public interface IPurchasePostService {

    PostedInvoiceSummary postDraft(Long draftId);
}
