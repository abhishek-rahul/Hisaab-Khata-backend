package com.hisaab_khata.hisaab_khata.repository;

import com.hisaab_khata.hisaab_khata.domain.Category;
import com.hisaab_khata.hisaab_khata.enums.CategoryScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByShop_IdAndScope(Long shopId, CategoryScope scope);

    List<Category> findByShop_Id(Long shopId);

    boolean existsByShop_IdAndScopeAndName(Long shopId, CategoryScope scope, String name);
}

