package com.hisaab_khata.hisaab_khata.auth;

import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("shopSecurity")
@RequiredArgsConstructor
public class ShopSecurity {

    private final ShopContext shopContext;

    /**
     * For use in @PreAuthorize SpEL: @shopSecurity.isCurrentShop(#shopId)
     */
    public boolean isCurrentShop(Long shopId) {
        if (shopId == null) {
            return false;
        }
        return shopId.equals(shopContext.getCurrentShopId());
    }
}
