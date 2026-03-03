package com.hisaab_khata.hisaab_khata.controller;


import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.khatadto.PendingKhataResponse;
import com.hisaab_khata.hisaab_khata.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/khata")
@RequiredArgsConstructor
public class KhataController {

    @Autowired
    private final IReportService reportService;

    @GetMapping("/pending")
    public ResponseEntity<SuccessResponse<List<PendingKhataResponse>>> pending() {
        List<PendingKhataResponse> res = reportService.getPendingKhataSummary();
        return ResponseEntity.ok(
                SuccessResponse.<List<PendingKhataResponse>>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }
}

