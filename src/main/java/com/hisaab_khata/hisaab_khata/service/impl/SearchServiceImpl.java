package com.hisaab_khata.hisaab_khata.service.impl;


//import com.hisaab_khata.hisaab_khata.domain.Category;
//import com.hisaab_khata.hisaab_khata.domain.Product;
import com.hisaab_khata.hisaab_khata.dto.stockdto.SearchResultResponse;
import com.hisaab_khata.hisaab_khata.repository.CategoryRepository;
import com.hisaab_khata.hisaab_khata.repository.ProductRepository;
import com.hisaab_khata.hisaab_khata.service.ISearchService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements ISearchService {

    @Autowired
    private final ProductRepository productRepo;

    @Autowired
    private final CategoryRepository categoryRepo;
    private final ShopContext shopContext;

    @Override
    public List<SearchResultResponse> search(String query) {

        Long shopId = shopContext.getCurrentShopId();

        List<SearchResultResponse> results = new ArrayList<>();

        productRepo.findByShopIdAndNameContainingIgnoreCase(shopId, query)
                .forEach(p -> results.add(
                        SearchResultResponse.builder()
                                .type("PRODUCT")
                                .id(p.getId())
                                .name(p.getName())
                                .build()
                ));

        categoryRepo.findByShop_Id(shopId)
                .stream()
                .filter(c -> c.getName().toLowerCase().contains(query.toLowerCase()))
                .forEach(c -> results.add(
                        SearchResultResponse.builder()
                                .type("CATEGORY")
                                .id(c.getId())
                                .name(c.getName())
                                .build()
                ));

        return results;
    }
}


