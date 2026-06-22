package com.formbuilder.controller;

import com.formbuilder.entity.FormSchema;
import com.formbuilder.entity.PublishedForm;
import com.formbuilder.repository.FormSchemaRepository;
import com.formbuilder.service.PublishedFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/published-forms")
public class PublishedFormController {

    @Autowired
    private PublishedFormService publishedFormService;

    @Autowired
    private FormSchemaRepository formSchemaRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll() {
        List<PublishedForm> list = publishedFormService.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", list);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status/{formId}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable Long formId) {
        Map<String, Object> result = new HashMap<>();
        Optional<PublishedForm> opt = publishedFormService.findByFormId(formId);
        result.put("success", true);
        if (opt.isPresent()) {
            result.put("data", opt.get());
        } else {
            result.put("data", null);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<Map<String, Object>> findByToken(@PathVariable String token) {
        Map<String, Object> result = new HashMap<>();
        Optional<PublishedForm> opt = publishedFormService.findByToken(token);
        if (!opt.isPresent()) {
            result.put("success", false);
            result.put("message", "表单不存在");
            return ResponseEntity.ok(result);
        }
        PublishedForm publishedForm = opt.get();
        if (!"PUBLISHED".equals(publishedForm.getStatus())) {
            result.put("success", false);
            result.put("message", "表单已下线");
            return ResponseEntity.ok(result);
        }
        Optional<FormSchema> formOpt = formSchemaRepository.findById(publishedForm.getFormId());
        if (!formOpt.isPresent()) {
            result.put("success", false);
            result.put("message", "表单数据不存在");
            return ResponseEntity.ok(result);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("publishedForm", publishedForm);
        data.put("formSchema", formOpt.get());
        result.put("success", true);
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/publish/{formId}")
    public ResponseEntity<Map<String, Object>> publish(@PathVariable Long formId) {
        Map<String, Object> result = new HashMap<>();
        try {
            PublishedForm published = publishedFormService.publishForm(formId);
            result.put("success", true);
            result.put("data", published);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/offline/{id}")
    public ResponseEntity<Map<String, Object>> offline(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            PublishedForm offline = publishedFormService.offlineForm(id);
            result.put("success", true);
            result.put("data", offline);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        publishedFormService.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }
}
