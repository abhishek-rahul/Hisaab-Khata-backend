package com.hisaab_khata.hisaab_khata.controller;


import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.TodaySummaryResponse;
import com.hisaab_khata.hisaab_khata.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    @Autowired
    private final IDashboardService dashboardService;

    @GetMapping("/today")
    public ResponseEntity<SuccessResponse<TodaySummaryResponse>> today() {
        TodaySummaryResponse res = dashboardService.getTodaySummary();
        return ResponseEntity.ok(
                SuccessResponse.<TodaySummaryResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping("/week")
    public ResponseEntity<SuccessResponse<TodaySummaryResponse>> week() {
        TodaySummaryResponse res = dashboardService.getWeekSummary();
        return ResponseEntity.ok(
                SuccessResponse.<TodaySummaryResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping("/month")
    public ResponseEntity<SuccessResponse<TodaySummaryResponse>> month() {
        TodaySummaryResponse res = dashboardService.getMonthSummary();
        return ResponseEntity.ok(
                SuccessResponse.<TodaySummaryResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }
}
