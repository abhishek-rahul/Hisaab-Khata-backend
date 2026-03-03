package com.hisaab_khata.hisaab_khata.service.impl;


//import com.hisaab_khata.hisaab_khata.domain.Customer;
import com.hisaab_khata.hisaab_khata.domain.CustomerLedger;
import com.hisaab_khata.hisaab_khata.domain.Sale;
import com.hisaab_khata.hisaab_khata.dto.khatadto.KhataReportResponse;
import com.hisaab_khata.hisaab_khata.dto.khatadto.PendingKhataResponse;
//import com.hisaab_khata.hisaab_khata.dto.reportdto.DailyProfitEntry;
import com.hisaab_khata.hisaab_khata.domain.DailySummary;
import com.hisaab_khata.hisaab_khata.dto.reportdto.DailyReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.DailyRangeReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.ProfitReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.SalesReportResponse;
import com.hisaab_khata.hisaab_khata.dto.reportdto.StockReportResponse;
import com.hisaab_khata.hisaab_khata.dto.stockdto.StockResponse;
import com.hisaab_khata.hisaab_khata.enums.LedgerType;
import com.hisaab_khata.hisaab_khata.mapper.ReportMapper;
import com.hisaab_khata.hisaab_khata.mapper.StockMapper;
import com.hisaab_khata.hisaab_khata.repository.*;
import com.hisaab_khata.hisaab_khata.service.IReportService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements IReportService {

    @Autowired
    private final SaleRepository saleRepo;

    @Autowired
    private final StockRepository stockRepo;

    @Autowired
    private final ProductRepository productRepo;

    @Autowired
    private final CustomerRepository customerRepo;

    @Autowired
    private final CustomerLedgerRepository ledgerRepo;

    @Autowired
    private final DailySummaryRepository dailySummaryRepository;

    private final ReportMapper mapper;
    private final StockMapper stockMapper;

    private final ShopContext shopContext;

    @Override
    public ProfitReportResponse getProfitReport(String from, String to) {

        Long shopId = shopContext.getCurrentShopId();

        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);

        List<Sale> sales = saleRepo.findByShopIdAndCreatedAtBetween(
                shopId,
                start.atStartOfDay(),
                end.atTime(23, 59, 59)
        );

        double totalProfit = sales.stream()
                .mapToDouble(Sale::getProfit)
                .sum();

        return ProfitReportResponse.builder()
                .totalProfit(totalProfit)
                .dailyBreakdown(
                        sales.stream().map(mapper::toProfitEntry).toList()
                )
                .build();
    }

    @Override
    public SalesReportResponse getSalesReport(String from, String to) {

        Long shopId = shopContext.getCurrentShopId();

        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);

        List<Sale> sales = saleRepo.findByShopIdAndCreatedAtBetween(
                shopId,
                start.atStartOfDay(),
                end.atTime(23, 59, 59)
        );

        double totalSales = sales.stream()
                .mapToDouble(s -> s.getTotalAmount())
                .sum();

        return SalesReportResponse.builder()
                .totalSales(totalSales)
                .dailyBreakdown(sales.stream()
                        .map(mapper::toSalesEntry)
                        .toList())
                .build();
    }

    @Override
    public StockReportResponse getStockReport() {

        Long shopId = shopContext.getCurrentShopId();

        List<StockResponse> stockList = stockRepo.findByShopProduct_Shop_Id(shopId)
                .stream()
                .map(stockMapper::toResponse)
                .toList();

        return StockReportResponse.builder()
                .stock(stockList)
                .build();
    }

    @Override
    public KhataReportResponse getKhataReport() {

        Long shopId = shopContext.getCurrentShopId();

        List<PendingKhataResponse> pending = customerRepo.findByShopId(shopId)
                .stream()
                .map(customer -> {
                    List<CustomerLedger> entries =
                            ledgerRepo.findByCustomerId(customer.getId());

                    double udhar = entries.stream()
                            .filter(e -> e.getType() == LedgerType.UDHAR_DIYA)
                            .mapToDouble(CustomerLedger::getAmount)
                            .sum();

                    double paid = entries.stream()
                            .filter(e -> e.getType() == LedgerType.JAMA_HUA)
                            .mapToDouble(CustomerLedger::getAmount)
                            .sum();

                    double balance = udhar - paid;
                    if (balance <= 0) return null;

                    return new PendingKhataResponse(
                            customer.getId(),
                            customer.getName(),
                            balance
                    );
                })
                .filter(x -> x != null)
                .toList();

        return KhataReportResponse.builder()
                .pendingKhata(pending)
                .build();
    }

    @Override
    public DailyReportResponse getDailyReport(LocalDate date) {
        Long shopId = shopContext.getCurrentShopId();
        return dailySummaryRepository.findByShop_IdAndDay(shopId, date)
                .map(this::toDailyReportResponse)
                .orElseGet(() -> DailyReportResponse.builder()
                        .date(date)
                        .totalSales(BigDecimal.ZERO)
                        .totalPurchase(BigDecimal.ZERO)
                        .cashIn(BigDecimal.ZERO)
                        .cashOut(BigDecimal.ZERO)
                        .receivable(BigDecimal.ZERO)
                        .payable(BigDecimal.ZERO)
                        .build());
    }

    @Override
    public DailyRangeReportResponse getDailyRangeReport(LocalDate from, LocalDate to) {
        Long shopId = shopContext.getCurrentShopId();
        List<DailySummary> summaries = dailySummaryRepository.findByShop_IdAndDayBetweenOrderByDayAsc(shopId, from, to);
        List<DailyReportResponse> days = summaries.stream().map(this::toDailyReportResponse).toList();
        BigDecimal totalSalesSum = BigDecimal.ZERO;
        BigDecimal totalPurchaseSum = BigDecimal.ZERO;
        BigDecimal cashInSum = BigDecimal.ZERO;
        BigDecimal cashOutSum = BigDecimal.ZERO;
        BigDecimal receivableSum = BigDecimal.ZERO;
        BigDecimal payableSum = BigDecimal.ZERO;
        for (DailySummary s : summaries) {
            totalSalesSum = totalSalesSum.add(nullSafe(s.getTotalSales()));
            totalPurchaseSum = totalPurchaseSum.add(nullSafe(s.getTotalPurchase()));
            cashInSum = cashInSum.add(nullSafe(s.getCashIn()));
            cashOutSum = cashOutSum.add(nullSafe(s.getCashOut()));
            receivableSum = receivableSum.add(nullSafe(s.getReceivable()));
            payableSum = payableSum.add(nullSafe(s.getPayable()));
        }
        return DailyRangeReportResponse.builder()
                .from(from)
                .to(to)
                .days(days)
                .totalSalesSum(totalSalesSum)
                .totalPurchaseSum(totalPurchaseSum)
                .cashInSum(cashInSum)
                .cashOutSum(cashOutSum)
                .receivableSum(receivableSum)
                .payableSum(payableSum)
                .build();
    }

    private DailyReportResponse toDailyReportResponse(DailySummary s) {
        return DailyReportResponse.builder()
                .date(s.getDay())
                .totalSales(nullSafe(s.getTotalSales()))
                .totalPurchase(nullSafe(s.getTotalPurchase()))
                .cashIn(nullSafe(s.getCashIn()))
                .cashOut(nullSafe(s.getCashOut()))
                .receivable(nullSafe(s.getReceivable()))
                .payable(nullSafe(s.getPayable()))
                .build();
    }

    private static BigDecimal nullSafe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}


