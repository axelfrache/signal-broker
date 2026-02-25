package com.axelfrache.signalbroker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsOverviewDto {
    private long totalTickets;
    private Map<String, Long> byPriority;
    private Map<String, Long> byCategory;
    private Double avgConfidence;
    private long last24hCount;
    private List<TimeSeriesPoint> series;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesPoint {
        private Double ts;
        private Long count;
    }
}
