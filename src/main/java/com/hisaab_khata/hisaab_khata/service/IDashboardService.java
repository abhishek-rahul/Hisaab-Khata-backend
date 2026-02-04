package com.hisaab_khata.hisaab_khata.service;


import com.hisaab_khata.hisaab_khata.dto.reportdto.TodaySummaryResponse;

public interface IDashboardService {

    TodaySummaryResponse getTodaySummary();

    TodaySummaryResponse getWeekSummary();

    TodaySummaryResponse getMonthSummary();
}

