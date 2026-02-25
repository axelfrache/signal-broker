package com.axelfrache.signalbroker.service;

import com.axelfrache.signalbroker.dto.PaginatedResponse;
import com.axelfrache.signalbroker.dto.StatsOverviewDto;
import com.axelfrache.signalbroker.dto.TicketDetailsDto;
import com.axelfrache.signalbroker.dto.TicketDto;
import com.axelfrache.signalbroker.model.entity.LabeledTicketEntity;
import com.axelfrache.signalbroker.repository.LabeledTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabeledTicketService {

    private final LabeledTicketRepository repository;

    public PaginatedResponse<TicketDto> getTickets(List<String> priorities, List<String> categories, String q,
            Double fromTime, Double toTime, int page, int size, String sortProperty, String sortDirection) {
        var direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        var pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));

        var validPriorities = priorities != null && !priorities.isEmpty() ? priorities : null;
        var validCategories = categories != null && !categories.isEmpty() ? categories : null;
        var qPattern = q != null && !q.trim().isEmpty() ? "%" + q + "%" : null;

        var ticketPage = repository.findWithFilters(validPriorities, validCategories, q, qPattern,
                fromTime, toTime, pageable);

        var items = ticketPage.getContent().stream().map(this::toDto).collect(Collectors.toList());
        var pageInfo = new PaginatedResponse.PageInfo(page, size, ticketPage.getTotalElements(),
                ticketPage.getTotalPages());

        return new PaginatedResponse<>(items, pageInfo);
    }

    public TicketDetailsDto getTicketById(String ticketId) {
        return repository.findById(ticketId).map(this::toDetailsDto).orElse(null);
    }

    public StatsOverviewDto getStats(Double fromTime, Double toTime) {
        if (toTime == null) {
            var maxTime = repository.findMaxReceivedAt();
            toTime = maxTime != null ? maxTime : (double) (System.currentTimeMillis() / 1000L);
        }
        if (fromTime == null) {
            var minTime = repository.findMinReceivedAt();
            fromTime = minTime != null ? minTime : toTime - (7 * 86400); // Default to all time if possible
        }

        var totalCount = repository.countByTimeRange(fromTime, toTime);
        var rangeInSeconds = toTime - fromTime;
        var bucketSize = 3600.0; // 1 hour by default

        if (rangeInSeconds > 86400 * 7) {
            bucketSize = 86400.0; // 1 day if range > 7 days
        }

        var priorityRows = repository.countByPriorityInTimeRange(fromTime, toTime);
        var byPriority = new HashMap<String, Long>();
        for (var row : priorityRows) {
            byPriority.put((String) row[0], ((Number) row[1]).longValue());
        }

        var categoryRows = repository.countByCategoryInTimeRange(fromTime, toTime);
        var byCategory = new HashMap<String, Long>();
        for (var row : categoryRows) {
            byCategory.put((String) row[0], ((Number) row[1]).longValue());
        }

        var avgConfidence = repository.getAverageConfidenceInTimeRange(fromTime, toTime);

        var last24hCount = repository.countByTimeRange(toTime - 86400, toTime);

        var seriesRows = repository.getTimeSeriesCounts(fromTime, toTime, bucketSize);
        var seriesMap = new HashMap<Double, Long>();
        for (var row : seriesRows) {
            seriesMap.put(((Number) row[0]).doubleValue(), ((Number) row[1]).longValue());
        }

        var series = new java.util.ArrayList<StatsOverviewDto.TimeSeriesPoint>();
        var currentBucket = Math.floor(fromTime / bucketSize) * bucketSize;
        var endBucket = Math.floor(toTime / bucketSize) * bucketSize;

        while (currentBucket <= endBucket) {
            series.add(new StatsOverviewDto.TimeSeriesPoint(currentBucket, seriesMap.getOrDefault(currentBucket, 0L)));
            currentBucket += bucketSize;
        }

        var stats = new StatsOverviewDto();
        stats.setTotalTickets(totalCount);
        stats.setByPriority(byPriority);
        stats.setByCategory(byCategory);
        stats.setAvgConfidence(avgConfidence != null ? avgConfidence : 0.0);
        stats.setLast24hCount(last24hCount);
        stats.setSeries(series);

        return stats;
    }

    private TicketDto toDto(LabeledTicketEntity entity) {
        var dto = new TicketDto();
        dto.setTicketId(entity.getTicketId());
        dto.setSubject(entity.getSubject());
        dto.setContact(entity.getContact());
        dto.setConfidence(entity.getConfidence());
        dto.setReceivedAt(entity.getReceivedAt());
        dto.setLabeledAt(entity.getLabeledAt());
        dto.setCategory(entity.getCategory());
        dto.setPriority(entity.getPriority());
        dto.setTicketType(entity.getTicketType());
        return dto;
    }

    private TicketDetailsDto toDetailsDto(LabeledTicketEntity entity) {
        var dto = new TicketDetailsDto();
        dto.setTicketId(entity.getTicketId());
        dto.setSubject(entity.getSubject());
        dto.setContact(entity.getContact());
        dto.setConfidence(entity.getConfidence());
        dto.setReceivedAt(entity.getReceivedAt());
        dto.setLabeledAt(entity.getLabeledAt());
        dto.setCategory(entity.getCategory());
        dto.setPriority(entity.getPriority());
        dto.setTicketType(entity.getTicketType());
        dto.setBody(entity.getBody());
        dto.setSchemaVersion(entity.getSchemaVersion());
        return dto;
    }
}
