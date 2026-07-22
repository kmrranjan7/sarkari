package com.sarkari.post.application.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContactResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String inquiryType;
    private String subject;
    private String message;
    private LocalDateTime createdAt;
}
