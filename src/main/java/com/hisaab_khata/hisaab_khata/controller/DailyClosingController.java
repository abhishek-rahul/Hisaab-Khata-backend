package com.hisaab_khata.hisaab_khata.controller;


import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.DailyClosingResponse;
import com.hisaab_khata.hisaab_khata.service.IDailyClosingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/closing")
@RequiredArgsConstructor
public class DailyClosingController {

    @Autowired
    private final IDailyClosingService closingService;

    @PostMapping
    public ResponseEntity<SuccessResponse<DailyClosingResponse>> close() {
        DailyClosingResponse res = closingService.performClosing();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.<DailyClosingResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<DailyClosingResponse>>> list() {
        List<DailyClosingResponse> res = closingService.getAllClosings();
        return ResponseEntity.ok(
                SuccessResponse.<List<DailyClosingResponse>>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }
}

