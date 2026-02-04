package com.hisaab_khata.hisaab_khata.service.impl;


import com.hisaab_khata.hisaab_khata.domain.DailyClosing;
import com.hisaab_khata.hisaab_khata.domain.Sale;
import com.hisaab_khata.hisaab_khata.dto.reportdto.TodaySummaryResponse;
import com.hisaab_khata.hisaab_khata.repository.*;
import com.hisaab_khata.hisaab_khata.service.IDashboardService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    @Autowired
    private final DailyClosingRepository dailyRepo;

    @Autowired
    private final SaleRepository saleRepository;

    private final ShopContext shopContext;

    @Override
    public TodaySummaryResponse getTodaySummary() {

        Long shopId = shopContext.getCurrentShopId();
        LocalDate today = LocalDate.now();

        // Use DailySalesEntry if exists (faster)
        DailyClosing entry = dailyRepo
                .findByShopIdAndClosingDate(shopId, today.toString());


        if (entry != null) {
            return TodaySummaryResponse.builder()
                    .date(today.toString())
                    .todaySales(entry.getTotalSales())
                    .cashCollected(entry.getCashCollected())
                    .upiCollected(entry.getUpiReceived())
                    .udharGiven(entry.getUdharGiven())
                    .todayProfit(entry.getProfit())
                    .build();
        }

        // Otherwise dynamically compute
        List<Sale> sales = saleRepository.findByShopId(shopId).stream()
                .filter(s -> s.getCreatedAt().toLocalDate().equals(today))
                .toList();

        Double totalSales = 0d;
        Double cash = 0d;
        Double upi = 0d;
        Double udhar = 0d;
        Double profit = 0d;

        for (Sale s : sales) {
            totalSales += s.getTotalAmount();
            cash += s.getCashPaid();
            upi += s.getUpiPaid();
            udhar += s.getUdharAmount();
            profit += s.getProfit();
        }

        return TodaySummaryResponse.builder()
                .date(today.toString())
                .todaySales(totalSales)
                .cashCollected(cash)
                .upiCollected(upi)
                .udharGiven(udhar)
                .todayProfit(profit)
                .build();
    }

    @Override
    public TodaySummaryResponse getWeekSummary() {
        return null;
    }

    @Override
    public TodaySummaryResponse getMonthSummary() {
        return null;
    }
}


