package com.sarkari.post.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreatePushSubscriptionRequest {

    @NotBlank(message = "Push token is required.")
    @Size(max = 1024, message = "Push token must be at most 1024 characters.")
    private String token;
}
