package com.hisaab_khata.hisaab_khata.controller;

import com.hisaab_khata.hisaab_khata.dto.ApiResponse;
import com.hisaab_khata.hisaab_khata.dto.purchaseupload.ParsedInvoiceResponse;
import com.hisaab_khata.hisaab_khata.exception.BusinessValidationException;
import com.hisaab_khata.hisaab_khata.service.IPurchaseUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseUploadController {

    private final IPurchaseUploadService purchaseUploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ParsedInvoiceResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "supplierPartyId", required = false) Long supplierPartyId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("File is required", "MISSING_FILE");
        }
        String originalFileName = file.getOriginalFilename();
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessValidationException("Failed to read file", "FILE_READ_ERROR");
        }
        ParsedInvoiceResponse response = purchaseUploadService.upload(
                bytes, originalFileName, supplierPartyId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Upload parsed successfully", response));
    }

    @GetMapping("/upload/{uploadId}")
    public ResponseEntity<ApiResponse<ParsedInvoiceResponse>> getUpload(@PathVariable Long uploadId) {
        ParsedInvoiceResponse response = purchaseUploadService.getByUploadId(uploadId);
        return ResponseEntity.ok(ApiResponse.ok("OK", response));
    }
}
