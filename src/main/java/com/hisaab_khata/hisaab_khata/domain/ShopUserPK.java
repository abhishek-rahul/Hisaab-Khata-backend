package com.hisaab_khata.hisaab_khata.domain;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopUserPK implements Serializable {

    private Long shopId;
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShopUserPK that = (ShopUserPK) o;
        return Objects.equals(shopId, that.shopId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shopId, userId);
    }
}
