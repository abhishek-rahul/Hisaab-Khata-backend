package com.hisaab_khata.hisaab_khata.controller;


import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.khatadto.KhataReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.ProfitReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.SalesReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.StockReportResponse;
import com.hisaab_khata.hisaab_khata.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

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
}

