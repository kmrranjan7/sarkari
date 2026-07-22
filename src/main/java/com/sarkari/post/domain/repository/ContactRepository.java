package com.sarkari.post.domain.repository;

import com.sarkari.post.domain.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}
