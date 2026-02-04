package com.hisaab_khata.hisaab_khata.util;


import org.springframework.stereotype.Component;

import java.time.*;

@Component
public class DateUtil {

    public LocalDateTime startOfToday() {
        return LocalDate.now().atStartOfDay();
    }

    public LocalDateTime endOfToday() {
        return LocalDate.now().atTime(23, 59, 59);
    }

    public LocalDateTime startOfWeek() {
        return LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
    }

    public LocalDateTime endOfWeek() {

        //return startOfWeek().plusDays(6).atTime(23, 59, 59); need to re-watch
        return null;
    }

    public LocalDateTime startOfMonth() {
        return LocalDate.now().withDayOfMonth(1).atStartOfDay();
    }

    public LocalDateTime endOfMonth() {
        return LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
                .atTime(23, 59, 59);
    }
}

