package com.sarkari.post.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateContactRequest {

    @NotBlank(message = "fullName is required")
    @Size(min = 2, max = 120, message = "fullName must be between 2 and 120 characters")
    private String fullName;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Size(max = 160, message = "email must not exceed 160 characters")
    private String email;

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "phone must be a valid 10-digit number")
    private String phone;

    @NotBlank(message = "inquiryType is required")
    @Size(min = 2, max = 60, message = "inquiryType must be between 2 and 60 characters")
    private String inquiryType;

    @NotBlank(message = "subject is required")
    @Size(min = 3, max = 180, message = "subject must be between 3 and 180 characters")
    private String subject;

    @NotBlank(message = "message is required")
    @Size(min = 10, max = 4000, message = "message must be between 10 and 4000 characters")
    private String message;
}
