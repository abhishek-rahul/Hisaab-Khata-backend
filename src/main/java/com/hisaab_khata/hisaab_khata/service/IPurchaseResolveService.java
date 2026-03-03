package com.hisaab_khata.hisaab_khata.service;

import com.hisaab_khata.hisaab_khata.dto.purchasedraft.DraftReviewResponse;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.ResolveLineRequest;

public interface IPurchaseResolveService {

    DraftReviewResponse runAutoResolve(Long draftId);

    DraftReviewResponse getReview(Long draftId);

    DraftReviewResponse resolveLine(Long draftId, Long lineId, ResolveLineRequest request);
}
