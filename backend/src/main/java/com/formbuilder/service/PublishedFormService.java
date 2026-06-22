package com.formbuilder.service;

import com.formbuilder.entity.FormSchema;
import com.formbuilder.entity.PublishedForm;
import com.formbuilder.repository.FormSchemaRepository;
import com.formbuilder.repository.PublishedFormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PublishedFormService {

    @Autowired
    private PublishedFormRepository publishedFormRepository;

    @Autowired
    private FormSchemaRepository formSchemaRepository;

    public List<PublishedForm> findAll() {
        return publishedFormRepository.findAllByOrderByPublishedAtDesc();
    }

    public Optional<PublishedForm> findByToken(String token) {
        return publishedFormRepository.findByToken(token);
    }

    public Optional<PublishedForm> findByFormId(Long formId) {
        return publishedFormRepository.findByFormIdAndStatus(formId, "PUBLISHED");
    }

    public PublishedForm publishForm(Long formId) {
        Optional<FormSchema> formOpt = formSchemaRepository.findById(formId);
        if (!formOpt.isPresent()) {
            throw new RuntimeException("表单不存在");
        }
        FormSchema formSchema = formOpt.get();

        Optional<PublishedForm> existingOpt = publishedFormRepository.findByFormIdAndStatus(formId, "PUBLISHED");
        if (existingOpt.isPresent()) {
            return existingOpt.get();
        }

        PublishedForm publishedForm = new PublishedForm();
        publishedForm.setFormId(formId);
        publishedForm.setFormName(formSchema.getName());
        publishedForm.setToken(UUID.randomUUID().toString().replace("-", ""));
        publishedForm.setStatus("PUBLISHED");
        publishedForm.setSubmitCount(0);
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
}
