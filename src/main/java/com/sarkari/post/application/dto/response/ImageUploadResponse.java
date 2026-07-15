package com.sarkari.post.application.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ImageUploadResponse {

    private String fileName;
    private String url;
    private String contentType;
    private long size;
}
