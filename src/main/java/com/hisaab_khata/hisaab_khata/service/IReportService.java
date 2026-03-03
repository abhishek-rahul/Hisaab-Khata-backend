package com.hisaab_khata.hisaab_khata.service;


import com.hisaab_khata.hisaab_khata.dto.khatadto.KhataReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.DailyReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.DailyRangeReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.ProfitReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.SalesReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.StockReportResponse;

import java.time.LocalDate;

public interface IReportService {

    ProfitReportResponse getProfitReport(String fromDate, String toDate);

    SalesReportResponse getSalesReport(String fromDate, String toDate);

    StockReportResponse getStockReport();

    KhataReportResponse getKhataReport();

    DailyReportResponse getDailyReport(LocalDate date);

    DailyRangeReportResponse getDailyRangeReport(LocalDate from, LocalDate to);
}
