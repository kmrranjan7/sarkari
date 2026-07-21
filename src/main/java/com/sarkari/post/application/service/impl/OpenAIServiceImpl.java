package com.sarkari.post.application.service.impl;

import com.sarkari.common.exception.BusinessException;
import com.sarkari.post.application.dto.response.OpenAIResponse;
import com.sarkari.post.application.service.OpenAIService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIServiceImpl implements OpenAIService {

    private static final String KEY_MODEL = "model";
    private static final String KEY_MESSAGES = "messages";
    private static final String KEY_ROLE = "role";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_CHOICES = "choices";
    private static final String KEY_MESSAGE = "message";

    private final RestClient.Builder restClientBuilder;
    private final Clock clock;

    @Value("${app.security.api-key:}")
    private String openAiApiKey;

    @Value("${app.openai.base-url:https://api.openai.com/v1}")
    private String openAiBaseUrl;

    @Value("${app.openai.model:gpt-4o-mini}")
    private String openAiModel;

    @Override
    public OpenAIResponse send(String message) {
        String sanitizedMessage = message == null ? "" : message.trim();
        if (sanitizedMessage.isBlank()) {
            throw new BusinessException("message is required");
        }

        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            throw new BusinessException("OpenAI API key is not configured");
        }

        RestClient client = restClientBuilder
                .baseUrl(openAiBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> requestBody = Map.of(
            KEY_MODEL, openAiModel,
            KEY_MESSAGES, List.of(
                Map.of(KEY_ROLE, "system", KEY_CONTENT, "You are a concise assistant."),
                Map.of(KEY_ROLE, "user", KEY_CONTENT, sanitizedMessage)
                )
        );

        try {
            Object rawResponse = client.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(Object.class);

            if (!(rawResponse instanceof Map<?, ?> responseMap)) {
                throw new BusinessException("Invalid response from OpenAI");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) responseMap;

            String aiText = extractAssistantMessage(response);

            return OpenAIResponse.builder()
                    .requestMessage(sanitizedMessage)
                    .responseMessage(aiText)
                    .respondedAt(LocalDateTime.now(clock))
                    .build();
        } catch (HttpStatusCodeException ex) {
            log.error("OpenAI HTTP error status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw new BusinessException("OpenAI request failed with status " + ex.getStatusCode().value());
        } catch (ResourceAccessException ex) {
            log.error("OpenAI connectivity error", ex);
            throw new BusinessException("Unable to connect to OpenAI service");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("OpenAI request failed", ex);
            throw new BusinessException("Unable to get response from OpenAI");
        }
    }

    private String extractAssistantMessage(Map<String, Object> response) {
        if (response == null) {
            throw new BusinessException("Empty response from OpenAI");
        }

        Object choicesObj = response.get(KEY_CHOICES);
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            throw new BusinessException("Invalid response from OpenAI");
        }

        Object firstChoice = choices.getFirst();
        if (!(firstChoice instanceof Map<?, ?> firstChoiceMap)) {
            throw new BusinessException("Invalid response from OpenAI");
        }

        Object messageObj = firstChoiceMap.get(KEY_MESSAGE);
        if (!(messageObj instanceof Map<?, ?> messageMap)) {
            throw new BusinessException("Invalid response from OpenAI");
        }

        Object contentObj = messageMap.get(KEY_CONTENT);
        if (!(contentObj instanceof String content) || content.isBlank()) {
            throw new BusinessException("Empty response content from OpenAI");
        }

        return content.trim();
    }
}
