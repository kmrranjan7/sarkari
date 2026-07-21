package com.sarkari.post.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenAIRequest {

    @NotBlank(message = "message is required")
    @Size(max = 500, message = "message must not exceed 500 chars")
    private String message;
}
