package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.PartyLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartyLedgerRepository extends JpaRepository<PartyLedger, Long> {

    List<PartyLedger> findByShopIdAndPartyIdOrderByCreatedAtAsc(Long shopId, Long partyId);
}
