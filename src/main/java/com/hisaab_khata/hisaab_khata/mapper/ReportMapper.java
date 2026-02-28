package com.hisaab_khata.hisaab_khata.mapper;


//import com.hisaab_khata.hisaab_khata.domain.DailyClosing;
import com.hisaab_khata.hisaab_khata.domain.Sale;
import com.hisaab_khata.hisaab_khata.dto.reportdto.DailyProfitEntry;
import com.hisaab_khata.hisaab_khata.mapper.config.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface ReportMapper {

    // Profit per day from Sale
    @Mapping(target = "date", source = "createdAt", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "profit", source = "profit")
    DailyProfitEntry toProfitEntry(Sale sale);

    // Sales amount per day from Sale
    @Mapping(target = "date", source = "createdAt", dateFormat = "yyyy-MM-dd")
    @Mapping(
            target = "profit",
            expression = "java(s.getTotalAmount())"
    )
    DailyProfitEntry toSalesEntry(Sale s);
}


