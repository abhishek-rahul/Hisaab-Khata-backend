package com.hisaab_khata.hisaab_khata.util;


import com.hisaab_khata.hisaab_khata.domain.*;
import org.springframework.stereotype.Component;

// Ensures multi-tenancy safety)
@Component
public class ValidationUtil {


    public void validateShopOwnership(Long currentShopId, Product product) {
        if (!product.getShop().getId().equals(currentShopId)) {
            throw new RuntimeException("Access denied for product");
        }
    }

    public void validateShopOwnership(Long currentShopId, Supplier supplier) {
        if (!supplier.getShop().getId().equals(currentShopId)) {
            throw new RuntimeException("Access denied for supplier");
        }
    }

    public void validateShopOwnership(Long currentShopId, Customer customer) {
        if (!customer.getShop().getId().equals(currentShopId)) {
            throw new RuntimeException("Access denied for customer");
        }
    }

    public void validateShopOwnership(Long currentShopId, Purchase purchase) {
        if (!purchase.getShop().getId().equals(currentShopId)) {
            throw new RuntimeException("Access denied for purchase");
        }
    }

    public void validateShopOwnership(Long currentShopId, Sale sale) {
        if (!sale.getShopId().equals(currentShopId)) {
            throw new RuntimeException("Access denied for sale");
        }
    }

    public void validateShopOwnership(Long currentShopId, Category category) {
        if (!category.getShop().getId().equals(currentShopId)) {
            throw new RuntimeException("Access denied for supplier");
        }
    }
}

