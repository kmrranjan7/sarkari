package com.sarkari.post.application.service.impl;

import com.sarkari.post.application.dto.request.CreateContactRequest;
import com.sarkari.post.application.dto.response.ContactResponse;
import com.sarkari.post.application.dto.response.PagedResponse;
import com.sarkari.post.application.service.ContactService;
import com.sarkari.post.domain.entity.Contact;
import com.sarkari.post.domain.repository.ContactRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;

    @Override
    public ContactResponse create(CreateContactRequest request) {
        Contact saved = contactRepository.save(Contact.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone().trim())
                .inquiryType(request.getInquiryType().trim())
                .subject(request.getSubject().trim())
                .message(request.getMessage().trim())
                .build());

        log.info("Contact saved id={} email={}", saved.getId(), saved.getEmail());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
        public PagedResponse<ContactResponse> getAll(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortDir) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Contact> contactPage = contactRepository.findAll(pageable);

        List<ContactResponse> content = contactPage.getContent()
            .stream()
                .map(this::toResponse)
                .toList();

        return PagedResponse.<ContactResponse>builder()
            .content(content)
            .page(contactPage.getNumber())
            .size(contactPage.getSize())
            .totalElements(contactPage.getTotalElements())
            .totalPages(contactPage.getTotalPages())
            .sort(sortBy + "," + sortDir)
            .first(contactPage.isFirst())
            .last(contactPage.isLast())
            .build();
    }

    private ContactResponse toResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .fullName(contact.getFullName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .inquiryType(contact.getInquiryType())
                .subject(contact.getSubject())
                .message(contact.getMessage())
                .createdAt(contact.getCreatedAt())
                .build();
    }

}
