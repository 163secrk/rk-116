package com.formbuilder.repository;

import com.formbuilder.entity.PublishedForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublishedFormRepository extends JpaRepository<PublishedForm, Long> {

    Optional<PublishedForm> findByToken(String token);

    List<PublishedForm> findByFormId(Long formId);

    Optional<PublishedForm> findByFormIdAndStatus(Long formId, String status);

    List<PublishedForm> findAllByOrderByPublishedAtDesc();
}
