package com.axelfrache.signalbroker.dto;

import java.util.List;
import java.util.Map;

public record StatsOverviewDto(
        long totalTickets,
        Map<String, Long> byPriority,
        Map<String, Long> byCategory,
        double avgConfidence,
        long last24hCount,
        List<TimeSeriesPoint> series) {

    public record TimeSeriesPoint(
            double ts,
            long count) {
    }
}
