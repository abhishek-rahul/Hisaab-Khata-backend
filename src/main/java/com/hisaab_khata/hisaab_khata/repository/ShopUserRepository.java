package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.ShopUser;
import com.hisaab_khata.hisaab_khata.domain.ShopUserPK;
import com.hisaab_khata.hisaab_khata.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopUserRepository extends JpaRepository<ShopUser, ShopUserPK> {
    List<ShopUser> findByUserIdAndStatus(Long userId, UserStatus status);
    Optional<ShopUser> findByShopIdAndUserId(Long shopId, Long userId);
}
