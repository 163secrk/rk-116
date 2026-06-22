package com.formbuilder.service;

import com.formbuilder.entity.FormSchema;
import com.formbuilder.entity.PublishedForm;
import com.formbuilder.entity.SubmissionRecord;
import com.formbuilder.repository.FormSchemaRepository;
import com.formbuilder.repository.PublishedFormRepository;
import com.formbuilder.repository.SubmissionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PublishedFormService {

    @Autowired
    private PublishedFormRepository publishedFormRepository;

    @Autowired
    private FormSchemaRepository formSchemaRepository;

    @Autowired
    private SubmissionRecordRepository submissionRecordRepository;

    public List<PublishedForm> findAll() {
        return publishedFormRepository.findAllByOrderByPublishedAtDesc();
    }

    public Optional<PublishedForm> findByToken(String token) {
        return publishedFormRepository.findByToken(token);
    }

    public Optional<PublishedForm> findByFormId(Long formId) {
        return publishedFormRepository.findByFormIdAndStatus(formId, "PUBLISHED");
    }

    public PublishedForm publishForm(Long formId, Map<String, Object> settings) {
        Optional<FormSchema> formOpt = formSchemaRepository.findById(formId);
        if (!formOpt.isPresent()) {
            throw new RuntimeException("表单不存在");
        }
        FormSchema formSchema = formOpt.get();

        LocalDateTime deadline = null;
        Integer maxSubmissionsPerPerson = null;
        String accessPassword = null;

        if (settings != null) {
            if (settings.get("deadline") != null && !((String) settings.get("deadline")).isEmpty()) {
                deadline = LocalDateTime.parse((String) settings.get("deadline"));
            }
            Object maxSub = settings.get("maxSubmissionsPerPerson");
            if (maxSub != null) {
                if (maxSub instanceof Number) {
                    maxSubmissionsPerPerson = ((Number) maxSub).intValue();
                } else if (maxSub instanceof String && !((String) maxSub).isEmpty()) {
                    try {
                        maxSubmissionsPerPerson = Integer.parseInt((String) maxSub);
                    } catch (NumberFormatException e) {
                    }
                }
            }
            Object pwd = settings.get("accessPassword");
            if (pwd != null && !((String) pwd).isEmpty()) {
                accessPassword = (String) pwd;
            }
        }

        Optional<PublishedForm> existingOpt = publishedFormRepository.findByFormIdAndStatus(formId, "PUBLISHED");
        if (existingOpt.isPresent()) {
            PublishedForm existing = existingOpt.get();
            existing.setFormName(formSchema.getName());
            existing.setDeadline(deadline);
            existing.setMaxSubmissionsPerPerson(maxSubmissionsPerPerson);
            existing.setAccessPassword(accessPassword);
            return publishedFormRepository.save(existing);
        }

        PublishedForm publishedForm = new PublishedForm();
        publishedForm.setFormId(formId);
        publishedForm.setFormName(formSchema.getName());
        publishedForm.setToken(UUID.randomUUID().toString().replace("-", ""));
        publishedForm.setStatus("PUBLISHED");
        publishedForm.setSubmitCount(0);
        publishedForm.setDeadline(deadline);
        publishedForm.setMaxSubmissionsPerPerson(maxSubmissionsPerPerson);
        publishedForm.setAccessPassword(accessPassword);
        return publishedFormRepository.save(publishedForm);
    }

    public PublishedForm offlineForm(Long id) {
        Optional<PublishedForm> opt = publishedFormRepository.findById(id);
        if (!opt.isPresent()) {
            throw new RuntimeException("发布记录不存在");
        }
        PublishedForm publishedForm = opt.get();
        publishedForm.setStatus("OFFLINE");
        publishedForm.setOfflineAt(LocalDateTime.now());
        return publishedFormRepository.save(publishedForm);
    }

    public void deleteById(Long id) {
        publishedFormRepository.deleteById(id);
    }

    public Map<String, Object> checkAndIncrementSubmitCount(String token, String visitorId) {
        Optional<PublishedForm> opt = publishedFormRepository.findByToken(token);
        if (!opt.isPresent()) {
            throw new RuntimeException("表单不存在");
        }
        PublishedForm publishedForm = opt.get();
        if (!"PUBLISHED".equals(publishedForm.getStatus())) {
            throw new RuntimeException("表单已下线");
        }

        if (publishedForm.getDeadline() != null && LocalDateTime.now().isAfter(publishedForm.getDeadline())) {
            throw new RuntimeException("表单已截止");
        }

        if (publishedForm.getMaxSubmissionsPerPerson() != null && publishedForm.getMaxSubmissionsPerPerson() > 0) {
            long count = submissionRecordRepository.countByPublishedFormIdAndVisitorId(publishedForm.getId(), visitorId);
            if (count >= publishedForm.getMaxSubmissionsPerPerson()) {
                throw new RuntimeException("已达上限");
            }
        }

        publishedForm.setSubmitCount((publishedForm.getSubmitCount() == null ? 0 : publishedForm.getSubmitCount()) + 1);
        PublishedForm saved = publishedFormRepository.save(publishedForm);

        SubmissionRecord record = new SubmissionRecord();
        record.setPublishedFormId(saved.getId());
        record.setVisitorId(visitorId);
        submissionRecordRepository.save(record);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("publishedForm", saved);
        return result;
    }

    public long getSubmissionCountForVisitor(Long publishedFormId, String visitorId) {
        return submissionRecordRepository.countByPublishedFormIdAndVisitorId(publishedFormId, visitorId);
    }
}
