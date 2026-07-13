package com.sarkari.post.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PostType {
    JOB("Job"),
    ADMIT("Admit"),
    EXAM("Exam"),
    RESULT("Result");

    private final String value;

    PostType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PostType fromValue(String input) {
        if (input == null || input.isBlank()) {
            return JOB;
        }
        for (PostType type : values()) {
            if (type.value.equalsIgnoreCase(input) || type.name().equalsIgnoreCase(input)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid postType: " + input);
    }
}
