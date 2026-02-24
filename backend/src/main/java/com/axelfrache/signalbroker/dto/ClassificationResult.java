package com.axelfrache.signalbroker.dto;

import com.axelfrache.signalbroker.model.enums.Category;
import com.axelfrache.signalbroker.model.enums.Priority;

import java.util.List;

public record ClassificationResult(
        Category category,
        Priority priority,
        List<String> labels,
        String summary,
        double confidence) {
}
