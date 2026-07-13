package com.sarkari.post.application.service;

import com.sarkari.post.application.dto.request.CreatePostRequest;
import com.sarkari.post.application.dto.request.UpdatePostRequest;
import com.sarkari.post.application.dto.response.PagedResponse;
import com.sarkari.post.application.dto.response.PostResponse;

public interface PostService {

    PostResponse create(CreatePostRequest request);

    PostResponse getByPostId(String postId);

    PagedResponse<PostResponse> getAll(String search, String postType, int page, int size, String sortBy, String sortDir);

    PostResponse update(String postId, UpdatePostRequest request);

    void delete(String postId);
}
