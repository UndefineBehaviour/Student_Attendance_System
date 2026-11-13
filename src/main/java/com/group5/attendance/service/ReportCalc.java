package com.group5.attendance.service;

import com.group5.attendance.model.AttendanceStat;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Pure calculations on lists of attendance stats (streams, lambdas, Map). No database here. */
public final class ReportCalc {

    private ReportCalc() {
    }

    /** Lowest percentage first; same percentage -> by roll number. */
    public static List<AttendanceStat> sortByPercent(List<AttendanceStat> stats) {
        return stats.stream()
                .sorted(Comparator.comparingDouble(AttendanceStat::getPercent)
                        .thenComparingInt(AttendanceStat::getRollNumber))
                .toList();
    }

    /**
     * Defaulters (below 75%) grouped by subject. Subjects with no lectures yet are ignored.
     * Inside each subject the lowest attendance comes first.
     */
    public static Map<String, List<AttendanceStat>> defaulters(List<AttendanceStat> stats) {
        return sortByPercent(stats).stream()
                .filter(AttendanceStat::isBelowThreshold)
                .collect(Collectors.groupingBy(AttendanceStat::getSubjectLabel, TreeMap::new, Collectors.toList()));
    }

    public static int countDefaulters(Map<String, List<AttendanceStat>> groups) {
        return groups.values().stream().mapToInt(List::size).sum();
    }

    public static int belowCount(List<AttendanceStat> stats) {
        return (int) stats.stream().filter(AttendanceStat::isBelowThreshold).count();
    }

    /** Class average in percent text, counting only students whose subject has lectures. */
    public static String average(List<AttendanceStat> stats) {
        return stats.stream()
                .filter(AttendanceStat::isHasLectures)
                .mapToDouble(AttendanceStat::getPercent)
                .average()
                .stream()
                .mapToObj(avg -> String.format(Locale.ROOT, "%.1f%%", avg))
                .findFirst()
                .orElse("-");
    }

    /** All subjects added together, as one stat (for the "Overall" figure). */
    public static AttendanceStat overall(List<AttendanceStat> stats) {
        AttendanceStat all = new AttendanceStat();
        all.setSubjectCode("ALL");
        all.setSubjectName("Overall");
        all.setAttended(stats.stream().mapToInt(AttendanceStat::getAttended).sum());
        all.setTotal(stats.stream().mapToInt(AttendanceStat::getTotal).sum());
        return all;
    }
}