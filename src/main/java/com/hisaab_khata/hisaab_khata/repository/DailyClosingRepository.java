package com.hisaab_khata.hisaab_khata.repository;


import com.hisaab_khata.hisaab_khata.domain.DailyClosing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//import java.util.List;

@Repository
public interface DailyClosingRepository extends JpaRepository<DailyClosing, Long> {

    DailyClosing findByShopIdAndClosingDate(Long shopId, String closingDate);

    boolean existsByShopIdAndClosingDate(Long shopId, String closingDate);
}

