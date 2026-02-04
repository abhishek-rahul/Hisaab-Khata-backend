package com.hisaab_khata.hisaab_khata.util;


import com.hisaab_khata.hisaab_khata.auth.ShopUserPrincipal;
import com.hisaab_khata.hisaab_khata.domain.Shop;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ShopContext {

    public Long getCurrentShopId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("No authentication found");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof ShopUserPrincipal sup) {
            return sup.getShopId();
        }

        throw new IllegalStateException("ShopId not found in security context");
    }
}

