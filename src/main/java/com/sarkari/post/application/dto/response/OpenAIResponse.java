package com.sarkari.post.application.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OpenAIResponse {

    private String requestMessage;
    private String responseMessage;
    private LocalDateTime respondedAt;
}
