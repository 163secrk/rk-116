package com.formbuilder.repository;

import com.formbuilder.entity.SubmissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRecordRepository extends JpaRepository<SubmissionRecord, Long> {

    long countByPublishedFormIdAndVisitorId(Long publishedFormId, String visitorId);
}
