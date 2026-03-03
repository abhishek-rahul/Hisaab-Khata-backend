package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.Category;
import com.hisaab_khata.hisaab_khata.enums.CategoryScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByShop_IdAndScope(Long shopId, CategoryScope scope);

    List<Category> findByShop_Id(Long shopId);

    Optional<Category> findByShop_IdAndId(Long shopId, Long id);

    boolean existsByShop_IdAndScopeAndName(Long shopId, CategoryScope scope, String name);
}

