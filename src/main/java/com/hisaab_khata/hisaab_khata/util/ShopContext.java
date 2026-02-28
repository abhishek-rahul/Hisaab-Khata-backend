package com.hisaab_khata.hisaab_khata.util;

import com.hisaab_khata.hisaab_khata.auth.ShopUserPrincipal;
import com.hisaab_khata.hisaab_khata.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ShopContext {

    public Long getCurrentShopId() {
        return getPrincipal().getShopId();
    }

    public Long getCurrentUserId() {
        return getPrincipal().getUserId();
    }

    public UserRole getCurrentRole() {
        return getPrincipal().getRole();
    }

    private ShopUserPrincipal getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("No authentication found");
        }
        if (auth.getPrincipal() instanceof ShopUserPrincipal sup) {
            return sup;
        }
        throw new IllegalStateException("ShopId not found in security context");
    }
}

