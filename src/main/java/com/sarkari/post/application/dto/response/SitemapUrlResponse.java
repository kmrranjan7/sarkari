package com.sarkari.post.application.dto.response;

import lombok.Builder;

@Builder
public record SitemapUrlResponse(
        String loc,
        String lastModified,
        String imageLoc
) {
}
