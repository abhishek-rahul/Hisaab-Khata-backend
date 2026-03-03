package com.hisaab_khata.hisaab_khata.mapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensures ReportMapper bean is available for ReportServiceImpl.
 * MapStruct generates ReportMapperImpl at compile time; when that impl is not
 * registered (e.g. after mvn clean install due to processor order), this backup
 * bean is used so the context loads.
 */
@Configuration
public class ReportMapperConfig {

    @Bean
    @ConditionalOnMissingBean(ReportMapper.class)
    public ReportMapper reportMapper() {
        return new ReportMapperImpl();
    }
}
