package com.echarge.modules.app.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AppScheduleTimeUtilTest {

    @Test
    void toUtcTimePeriodsMergesMidnightBoundaryForFirmware() {
        List<List<String>> localPeriods = List.of(List.of("04:00", "17:00"));

        List<List<String>> result = AppScheduleTimeUtil.toUtcTimePeriods(localPeriods, "Asia/Shanghai");

        assertEquals(List.of(List.of("20:00", "09:00")), result);
        assertNoEndOfDayLiteral(result);
    }

    @Test
    void toUtcTimePeriodsReplacesEndOfDayWithMidnight() {
        List<List<String>> localPeriods = List.of(List.of("04:00", "08:00"));

        List<List<String>> result = AppScheduleTimeUtil.toUtcTimePeriods(localPeriods, "Asia/Shanghai");

        assertEquals(List.of(List.of("20:00", "00:00")), result);
        assertNoEndOfDayLiteral(result);
    }

    @Test
    void toUtcTimePeriodsPreservesOtherPeriodsWhenJoiningBoundarySegments() {
        List<List<String>> localPeriods = List.of(
                List.of("08:00", "17:00"),
                List.of("02:11", "02:12"),
                List.of("04:00", "08:00"));

        List<List<String>> result = AppScheduleTimeUtil.toUtcTimePeriods(localPeriods, "Asia/Shanghai");

        assertEquals(List.of(
                List.of("18:11", "18:12"),
                List.of("20:00", "09:00")), result);
        assertNoEndOfDayLiteral(result);
    }

    private static void assertNoEndOfDayLiteral(List<List<String>> periods) {
        assertFalse(periods.stream().flatMap(List::stream).anyMatch("24:00"::equals));
    }
}
