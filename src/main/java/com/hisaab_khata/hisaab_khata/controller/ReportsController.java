package com.hisaab_khata.hisaab_khata.controller;


import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.khatadto.KhataReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.DailyReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.DailyRangeReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.ProfitReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.SalesReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.StockReportResponse;
import com.hisaab_khata.hisaab_khata.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportsController {

    @Autowired
    private final IReportService reportService;

    @GetMapping("/profit")
    public ResponseEntity<SuccessResponse<ProfitReportResponse>> profit(
            @RequestParam String from,
            @RequestParam String to
    ) {
        ProfitReportResponse res = reportService.getProfitReport(from, to);
        return ResponseEntity.ok(
                SuccessResponse.<ProfitReportResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping("/sales")
    public ResponseEntity<SuccessResponse<SalesReportResponse>> sales(
            @RequestParam String from,
            @RequestParam String to
    ) {
        SalesReportResponse res = reportService.getSalesReport(from, to);
        return ResponseEntity.ok(
                SuccessResponse.<SalesReportResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping("/stock")
    public ResponseEntity<SuccessResponse<StockReportResponse>> stock() {
        StockReportResponse res = reportService.getStockReport();
        return ResponseEntity.ok(
                SuccessResponse.<StockReportResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping("/khata")
    public ResponseEntity<SuccessResponse<KhataReportResponse>> khata() {
        KhataReportResponse res = reportService.getKhataReport();
        return ResponseEntity.ok(
                SuccessResponse.<KhataReportResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping("/daily")
    public ResponseEntity<SuccessResponse<DailyReportResponse>> daily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyReportResponse res = reportService.getDailyReport(date);
        return ResponseEntity.ok(
                SuccessResponse.<DailyReportResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping("/range")
    public ResponseEntity<SuccessResponse<DailyRangeReportResponse>> range(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        DailyRangeReportResponse res = reportService.getDailyRangeReport(from, to);
        return ResponseEntity.ok(
                SuccessResponse.<DailyRangeReportResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }
}

