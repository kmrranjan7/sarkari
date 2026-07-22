package com.sarkari.post.application.service;

import com.sarkari.post.application.dto.request.CreateContactRequest;
import com.sarkari.post.application.dto.response.ContactResponse;
import com.sarkari.post.application.dto.response.PagedResponse;

public interface ContactService {

    ContactResponse create(CreateContactRequest request);

    PagedResponse<ContactResponse> getAll(int page, int size, String sortBy, String sortDir);
}
