package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.Party;
import com.hisaab_khata.hisaab_khata.enums.PartyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {

    List<Party> findByShopId(Long shopId);

    List<Party> findByShopIdAndType(Long shopId, PartyType type);

    Optional<Party> findByShopIdAndId(Long shopId, Long id);

    boolean existsByShopIdAndPhoneAndIdNot(Long shopId, String phone, Long excludeId);

    boolean existsByShopIdAndPhone(Long shopId, String phone);
}
