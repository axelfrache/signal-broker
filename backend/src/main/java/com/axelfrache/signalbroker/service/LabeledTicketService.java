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
import java.util.Optional;

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
        var qPattern = q != null && !q.isBlank() ? "%" + q.trim().toLowerCase() + "%" : "%";

        var ticketPage = repository.findWithFilters(validPriorities, validCategories, qPattern,
                fromTime, toTime, pageable);

        var items = ticketPage.getContent().stream().map(this::toDto).toList();
        var pageInfo = new PaginatedResponse.PageInfo(page, size, ticketPage.getTotalElements(),
                ticketPage.getTotalPages());

        return new PaginatedResponse<>(items, pageInfo);
    }

    public Optional<TicketDetailsDto> getTicketById(String ticketId) {
        return repository.findById(ticketId).map(this::toDetailsDto);
    }

    public StatsOverviewDto getStats(Double fromTime, Double toTime) {
        if (toTime == null) {
            var maxTime = repository.findMaxReceivedAt();
            toTime = maxTime != null ? maxTime : (double) (System.currentTimeMillis() / 1000L);
        }
        if (fromTime == null) {
            var minTime = repository.findMinReceivedAt();
            fromTime = minTime != null ? minTime : toTime - (7 * 86400);
        }

        var totalCount = repository.countByTimeRange(fromTime, toTime);
        var rangeInSeconds = toTime - fromTime;
        var bucketSize = 3600.0;

        if (rangeInSeconds > 86400 * 7) {
            bucketSize = 86400.0;
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

        return new StatsOverviewDto(
                totalCount,
                byPriority,
                byCategory,
                avgConfidence != null ? avgConfidence : 0.0,
                last24hCount,
                series);
    }

    private TicketDto toDto(LabeledTicketEntity entity) {
        return new TicketDto(
                entity.getTicketId(),
                entity.getSubject(),
                entity.getContact(),
                entity.getConfidence(),
                entity.getReceivedAt(),
                entity.getLabeledAt(),
                entity.getCategory(),
                entity.getPriority(),
                entity.getTicketType(),
                entity.getCommonId());
    }

    private TicketDetailsDto toDetailsDto(LabeledTicketEntity entity) {
        return new TicketDetailsDto(
                entity.getTicketId(),
                entity.getSubject(),
                entity.getContact(),
                entity.getConfidence(),
                entity.getReceivedAt(),
                entity.getLabeledAt(),
                entity.getCategory(),
                entity.getPriority(),
                entity.getTicketType(),
                entity.getCommonId(),
                entity.getBody(),
                entity.getSchemaVersion());
    }
}
