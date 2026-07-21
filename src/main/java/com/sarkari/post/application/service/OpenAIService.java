package com.sarkari.post.application.service;

import com.sarkari.post.application.dto.response.OpenAIResponse;

public interface OpenAIService {

    OpenAIResponse send(String message);
}
