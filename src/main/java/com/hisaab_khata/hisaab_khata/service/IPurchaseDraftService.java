package com.hisaab_khata.hisaab_khata.service;

import com.hisaab_khata.hisaab_khata.dto.purchasedraft.CreateDraftRequest;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.DraftResponse;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.SaveDraftRequest;

public interface IPurchaseDraftService {

    DraftResponse createDraft(CreateDraftRequest request);

    DraftResponse getDraft(Long draftId);

    DraftResponse saveDraft(Long draftId, SaveDraftRequest request);
}
