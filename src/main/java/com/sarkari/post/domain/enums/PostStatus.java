package com.sarkari.post.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PostStatus {
    DRAFT("Draft"),
    PENDING_REVIEW("Pending Review"),
    SCHEDULED("Scheduled"),
    PUBLISHED("Published");

    private final String value;

    PostStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PostStatus fromValue(String input) {
        if (input == null || input.isBlank()) {
            return DRAFT;
        }
        for (PostStatus status : values()) {
            if (status.value.equalsIgnoreCase(input) || status.name().equalsIgnoreCase(input.replace(" ", "_"))) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid postStatus: " + input);
    }
}
