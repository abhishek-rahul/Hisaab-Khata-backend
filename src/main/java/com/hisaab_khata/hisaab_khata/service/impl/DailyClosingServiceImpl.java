package com.hisaab_khata.hisaab_khata.service.impl;


import com.hisaab_khata.hisaab_khata.domain.DailyClosing;
import com.hisaab_khata.hisaab_khata.domain.Sale;
import com.hisaab_khata.hisaab_khata.domain.Shop;
import com.hisaab_khata.hisaab_khata.dto.reportdto.DailyClosingResponse;
import com.hisaab_khata.hisaab_khata.enums.PaymentMode;
import com.hisaab_khata.hisaab_khata.exception.BusinessValidationException;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.mapper.DailyClosingMapper;
import com.hisaab_khata.hisaab_khata.repository.DailyClosingRepository;
import com.hisaab_khata.hisaab_khata.repository.SaleRepository;
import com.hisaab_khata.hisaab_khata.repository.ShopRepository;
import com.hisaab_khata.hisaab_khata.service.IDailyClosingService;
import com.hisaab_khata.hisaab_khata.util.DateUtil;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyClosingServiceImpl implements IDailyClosingService {

    @Autowired
    private final SaleRepository saleRepository;

    @Autowired
    private final DailyClosingRepository dailyRepo;

    @Autowired
    private final ShopContext shopContext;

    @Autowired
    private final ShopRepository shopRepository;


    @Override
    @Transactional
    public DailyClosingResponse performClosing() {
        Long shopId = shopContext.getCurrentShopId();
        LocalDate today = LocalDate.now();

        boolean alreadyClosed =
                dailyRepo.existsByShopIdAndClosingDate(shopId, today.toString());

        if (alreadyClosed) {
            throw new BusinessValidationException(
                    "Closing already done",
                    "CLOSING_ALREADY_DONE"
            );
        }

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

        DailyClosing entry = DailyClosing.builder()
                .shop(shopRepository.getReferenceById(shopId))
                .closingDate(today.toString())
                .totalSales(totalSales)
                .cashCollected(cash)
                .upiReceived(upi)
                .udharGiven(udhar)
                .profit(profit)
                .build();

        dailyRepo.save(entry);

        return DailyClosingResponse.builder()
                .closingDate(today.toString())
                .totalSales(totalSales)
                .cashCollected(cash)
                .upiCollected(upi)
                .udharGiven(udhar)
                .totalProfit(profit)
                .build();
    }

    @Override
    public List<DailyClosingResponse> getAllClosings() {
        return null;
    }
}



