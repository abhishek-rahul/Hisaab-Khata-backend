package com.hisaab_khata.hisaab_khata.controller;

import com.hisaab_khata.hisaab_khata.dto.ApiResponse;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.CreateDraftRequest;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.DraftResponse;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.DraftReviewResponse;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.ResolveLineRequest;
import com.hisaab_khata.hisaab_khata.dto.purchasedraft.SaveDraftRequest;
import com.hisaab_khata.hisaab_khata.service.IPurchaseDraftService;
import com.hisaab_khata.hisaab_khata.service.IPurchaseResolveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseDraftController {

    private final IPurchaseDraftService purchaseDraftService;
    private final IPurchaseResolveService purchaseResolveService;

    @PostMapping("/drafts")
    public ResponseEntity<ApiResponse<DraftResponse>> createDraft(@RequestBody @Valid CreateDraftRequest request) {
        DraftResponse response = purchaseDraftService.createDraft(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Draft created successfully", response));
    }

    @GetMapping("/drafts/{draftId}")
    public ResponseEntity<ApiResponse<DraftResponse>> getDraft(@PathVariable Long draftId) {
        DraftResponse response = purchaseDraftService.getDraft(draftId);
        return ResponseEntity.ok(ApiResponse.ok("OK", response));
    }

    @PutMapping("/drafts/{draftId}")
    public ResponseEntity<ApiResponse<DraftResponse>> saveDraft(
            @PathVariable Long draftId,
            @RequestBody @Valid SaveDraftRequest request) {
        DraftResponse response = purchaseDraftService.saveDraft(draftId, request);
        return ResponseEntity.ok(ApiResponse.ok("Draft saved", response));
    }

    @PostMapping("/drafts/{draftId}/resolve/auto")
    public ResponseEntity<ApiResponse<DraftReviewResponse>> runAutoResolve(@PathVariable Long draftId) {
        DraftReviewResponse response = purchaseResolveService.runAutoResolve(draftId);
        return ResponseEntity.ok(ApiResponse.ok("Auto-resolve completed", response));
    }

    @GetMapping("/drafts/{draftId}/review")
    public ResponseEntity<ApiResponse<DraftReviewResponse>> getReview(@PathVariable Long draftId) {
        DraftReviewResponse response = purchaseResolveService.getReview(draftId);
        return ResponseEntity.ok(ApiResponse.ok("OK", response));
    }

    @PutMapping("/drafts/{draftId}/lines/{lineId}/resolve")
    public ResponseEntity<ApiResponse<DraftReviewResponse>> resolveLine(
            @PathVariable Long draftId,
            @PathVariable Long lineId,
            @RequestBody @Valid ResolveLineRequest request) {
        DraftReviewResponse response = purchaseResolveService.resolveLine(draftId, lineId, request);
        return ResponseEntity.ok(ApiResponse.ok("Line resolved", response));
    }
}
