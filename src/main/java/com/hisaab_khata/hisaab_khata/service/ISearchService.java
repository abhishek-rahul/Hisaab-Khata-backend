package com.hisaab_khata.hisaab_khata.service;



import com.hisaab_khata.hisaab_khata.dto.stockdto.SearchResultResponse;

import java.util.List;

public interface ISearchService {

    List<SearchResultResponse> search(String query);
}
