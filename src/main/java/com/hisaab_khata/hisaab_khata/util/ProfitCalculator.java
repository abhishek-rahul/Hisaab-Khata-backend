package com.hisaab_khata.hisaab_khata.util;


import org.springframework.stereotype.Component;

@Component
public class ProfitCalculator {

    public double computeProfit(double sellingPrice, double latestCostPrice, double qty) {
        return (sellingPrice - latestCostPrice) * qty;
    }
}

