package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.PartyLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PartyLedgerRepository extends JpaRepository<PartyLedger, Long> {

    List<PartyLedger> findByShopIdAndPartyIdOrderByCreatedAtAsc(Long shopId, Long partyId);

    @Query("SELECT COALESCE(SUM(e.drAmount - e.crAmount), 0) FROM PartyLedger e WHERE e.shopId = :shopId AND e.party.id = :partyId AND e.createdAt < :before")
    BigDecimal sumDrMinusCrBefore(@Param("shopId") Long shopId, @Param("partyId") Long partyId, @Param("before") LocalDateTime before);

    Page<PartyLedger> findByShopIdAndParty_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAscIdAsc(
            Long shopId, Long partyId, LocalDateTime fromInclusive, LocalDateTime toExclusive, Pageable pageable);
}
