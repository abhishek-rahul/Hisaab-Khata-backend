package com.hisaab_khata.hisaab_khata.controller;


import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.stockdto.SearchResultResponse;
import com.hisaab_khata.hisaab_khata.service.ISearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    @Autowired
    private final ISearchService searchService;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<SearchResultResponse>>> search(
            @RequestParam String query
    ) {
        List<SearchResultResponse> res = searchService.search(query);
        return ResponseEntity.ok(
                SuccessResponse.<List<SearchResultResponse>>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }
}

