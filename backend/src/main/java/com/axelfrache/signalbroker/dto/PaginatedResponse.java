package com.axelfrache.signalbroker.dto;

import java.util.List;

public record PaginatedResponse<T>(
        List<T> items,
        PageInfo page) {

    public record PageInfo(
            int page,
            int size,
            long totalItems,
            int totalPages) {
    }
}
