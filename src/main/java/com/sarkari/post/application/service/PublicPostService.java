package com.sarkari.post.application.service;

import com.sarkari.post.application.dto.response.PagedResponse;
import com.sarkari.post.application.dto.response.PostResponse;
import java.util.List;
import java.util.Map;

public interface PublicPostService {

    PagedResponse<Map<String, Object>> getPublicJobs(String search, String postType, String postStatus, int page, int size, String sortBy, String sortDir);

    PagedResponse<Map<String, Object>> getPublicPostsByStatus(String postStatus, int page, int size, String sortBy, String sortDir);

    List<PostResponse> getPublicPostsBySlugAndStatus(String slug, String postStatus);
}