package com.formbuilder.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "submission_record")
public class SubmissionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "published_form_id", nullable = false)
    private Long publishedFormId;

    @Column(name = "visitor_id", nullable = false, length = 100)
    private String visitorId;

    @Column(name = "submit_time")
    private LocalDateTime submitTime;

    @PrePersist
    protected void onCreate() {
        submitTime = LocalDateTime.now();
    }
}
