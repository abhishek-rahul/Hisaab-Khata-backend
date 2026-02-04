package com.hisaab_khata.hisaab_khata.service;



import com.hisaab_khata.hisaab_khata.dto.reportdto.DailyClosingResponse;

import java.util.List;

public interface IDailyClosingService {

    DailyClosingResponse performClosing();

    List<DailyClosingResponse> getAllClosings();
}

