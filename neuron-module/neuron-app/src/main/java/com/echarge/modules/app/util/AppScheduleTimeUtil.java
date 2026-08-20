package com.echarge.modules.app.util;

import com.echarge.modules.app.entity.AppUser;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Converts date-less daily charging periods between the App user's zone and UTC.
 */
public final class AppScheduleTimeUtil {
    public static final String DEFAULT_ZONE_ID = "Asia/Shanghai";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private AppScheduleTimeUtil() {
    }

    public static String normalizeZoneId(String zoneId) {
        if (StringUtils.isBlank(zoneId)) {
            return DEFAULT_ZONE_ID;
        }
        String trimmed = zoneId.trim();
        try {
            return ZoneId.of(trimmed).getId();
        } catch (Exception e) {
            return DEFAULT_ZONE_ID;
        }
    }

    public static String userZoneId(AppUser user) {
        return normalizeZoneId(user != null ? user.getTimezone() : null);
    }

    public static List<List<String>> toUtcTimePeriods(Object rawPeriods, String userZoneId) {
        return convertTimePeriods(rawPeriods, normalizeZoneId(userZoneId), ZoneOffset.UTC.getId(), 10);
    }

    public static List<List<String>> fromUtcTimePeriods(Object rawPeriods, String userZoneId) {
        return convertTimePeriods(rawPeriods, ZoneOffset.UTC.getId(), normalizeZoneId(userZoneId), 24);
    }

    public static List<List<String>> normalizeTimePeriods(Object rawPeriods, int maxInputPeriods) {
        List<Period> periods = parsePeriods(rawPeriods, maxInputPeriods);
        List<int[]> intervals = new ArrayList<>();
        for (Period period : periods) {
            addClockInterval(intervals, period.startMinute(), period.endMinute());
        }
        return formatIntervals(mergeIntervals(intervals));
    }

    private static List<List<String>> convertTimePeriods(Object rawPeriods, String fromZoneId, String toZoneId, int maxInputPeriods) {
        List<Period> periods = parsePeriods(rawPeriods, maxInputPeriods);
        ZoneId fromZone = ZoneId.of(fromZoneId);
        ZoneId toZone = ZoneId.of(toZoneId);
        LocalDate baseDate = LocalDate.now(fromZone);
        List<int[]> intervals = new ArrayList<>();

        for (Period period : periods) {
            ZonedDateTime fromStart = atMinute(fromZone, baseDate, period.startMinute(), 0);
            int endDayOffset = period.endMinute() <= period.startMinute() ? 1 : 0;
            ZonedDateTime fromEnd = atMinute(fromZone, baseDate, period.endMinute(), endDayOffset);

            ZonedDateTime toStart = fromStart.withZoneSameInstant(toZone);
            ZonedDateTime toEnd = fromEnd.withZoneSameInstant(toZone);
            int startMinute = toMinute(toStart.toLocalTime());
            int endMinute = toMinute(toEnd.toLocalTime());

            if (toEnd.toLocalDate().isAfter(toStart.toLocalDate()) || endMinute <= startMinute) {
                intervals.add(new int[] {startMinute, 1440});
                intervals.add(new int[] {0, endMinute});
            } else {
                intervals.add(new int[] {startMinute, endMinute});
            }
        }

        return formatIntervals(mergeIntervals(intervals));
    }

    private static ZonedDateTime atMinute(ZoneId zone, LocalDate baseDate, int minute, int dayOffset) {
        if (minute == 1440) {
            return baseDate.plusDays(dayOffset + 1L).atStartOfDay(zone);
        }
        return baseDate.plusDays(dayOffset).atTime(minute / 60, minute % 60).atZone(zone);
    }

    private static List<Period> parsePeriods(Object rawPeriods, int maxInputPeriods) {
        if (rawPeriods == null) {
            return List.of();
        }
        if (!(rawPeriods instanceof List<?> rawList)) {
            throw new IllegalArgumentException("timePeriods must be an array");
        }
        if (rawList.size() > maxInputPeriods) {
            throw new IllegalArgumentException("timePeriods cannot exceed " + maxInputPeriods + " periods");
        }

        List<Period> periods = new ArrayList<>();
        for (Object item : rawList) {
            if (!(item instanceof List<?> pair) || pair.size() != 2) {
                throw new IllegalArgumentException("each time period must be [startTime, endTime]");
            }
            int start = parseMinute(pair.get(0), false);
            int end = parseMinute(pair.get(1), true);
            periods.add(new Period(start, end));
        }
        return periods;
    }

    private static int parseMinute(Object value, boolean allowEndOfDay) {
        if (!(value instanceof String text) || text.length() != 5 || text.charAt(2) != ':') {
            throw new IllegalArgumentException("time must be HH:mm");
        }
        int hour;
        int minute;
        try {
            hour = Integer.parseInt(text.substring(0, 2));
            minute = Integer.parseInt(text.substring(3, 5));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("time must be HH:mm");
        }
        if (hour == 24 && minute == 0 && allowEndOfDay) {
            return 1440;
        }
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException("time must be between 00:00 and 24:00");
        }
        return hour * 60 + minute;
    }

    private static void addClockInterval(List<int[]> intervals, int startMinute, int endMinute) {
        if (endMinute <= startMinute) {
            intervals.add(new int[] {startMinute, 1440});
            intervals.add(new int[] {0, endMinute});
        } else {
            intervals.add(new int[] {startMinute, endMinute});
        }
    }

    private static List<int[]> mergeIntervals(List<int[]> intervals) {
        if (intervals.isEmpty()) {
            return List.of();
        }
        intervals.sort(Comparator.comparingInt(interval -> interval[0]));
        List<int[]> merged = new ArrayList<>();
        for (int[] interval : intervals) {
            if (interval[0] == interval[1]) {
                continue;
            }
            if (merged.isEmpty() || interval[0] > merged.get(merged.size() - 1)[1]) {
                merged.add(new int[] {interval[0], interval[1]});
            } else {
                merged.get(merged.size() - 1)[1] = Math.max(merged.get(merged.size() - 1)[1], interval[1]);
            }
        }
        return merged;
    }

    private static List<List<String>> formatIntervals(List<int[]> intervals) {
        List<List<String>> result = new ArrayList<>();
        for (int[] interval : intervals) {
            result.add(List.of(formatMinute(interval[0]), formatMinute(interval[1])));
        }
        return result;
    }

    private static int toMinute(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    private static String formatMinute(int minute) {
        if (minute == 1440) {
            return "24:00";
        }
        return LocalTime.of(minute / 60, minute % 60).format(TIME_FORMATTER);
    }

    private record Period(int startMinute, int endMinute) {
    }
}
