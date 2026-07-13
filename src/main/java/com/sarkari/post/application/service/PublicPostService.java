package com.sarkari.post.application.service;

import com.sarkari.post.application.dto.response.PagedResponse;
import java.util.Map;

public interface PublicPostService {

    PagedResponse<Map<String, Object>> getPublicJobs(String search, String postType, String postStatus, int page, int size, String sortBy, String sortDir);
}