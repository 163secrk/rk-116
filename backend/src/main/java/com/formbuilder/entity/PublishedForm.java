package com.formbuilder.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "published_form")
public class PublishedForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id", nullable = false)
    private Long formId;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "form_name", length = 200)
    private String formName;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "offline_at")
    private LocalDateTime offlineAt;

    @Column(nullable = false, length = 20)
    private String status = "PUBLISHED";

    @Column(name = "submit_count")
    private Integer submitCount = 0;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "max_submissions_per_person")
    private Integer maxSubmissionsPerPerson;

    @Column(name = "access_password", length = 100)
    private String accessPassword;

    @PrePersist
    protected void onCreate() {
        publishedAt = LocalDateTime.now();
    }
}
