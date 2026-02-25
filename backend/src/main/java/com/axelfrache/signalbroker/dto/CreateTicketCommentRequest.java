package com.axelfrache.signalbroker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketCommentRequest(
        @NotBlank(message = "Author name is required") @Size(min = 2, max = 64, message = "Author name must be between 2 and 64 characters") String authorName,

        @NotBlank(message = "Body is required") @Size(min = 1, max = 2000, message = "Body must be between 1 and 2000 characters") String body) {
    public CreateTicketCommentRequest {
        if (authorName != null)
            authorName = authorName.trim();
        if (body != null)
            body = body.trim();
    }
}
