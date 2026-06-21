package com.formbuilder.controller;

import com.formbuilder.entity.FormSchema;
import com.formbuilder.service.FormSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/forms")
public class FormSchemaController {

    @Autowired
    private FormSchemaService formSchemaService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll() {
        List<FormSchema> list = formSchemaService.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", list);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {
        Optional<FormSchema> optional = formSchemaService.findById(id);
        Map<String, Object> result = new HashMap<>();
        if (optional.isPresent()) {
            result.put("success", true);
            result.put("data", optional.get());
        } else {
            result.put("success", false);
            result.put("message", "Form not found");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@RequestBody FormSchema formSchema) {
        FormSchema saved = formSchemaService.save(formSchema);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", saved);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody FormSchema formSchema) {
        Optional<FormSchema> optional = formSchemaService.findById(id);
        Map<String, Object> result = new HashMap<>();
        if (optional.isPresent()) {
            FormSchema existing = optional.get();
            existing.setName(formSchema.getName());
            existing.setSchemaData(formSchema.getSchemaData());
            FormSchema updated = formSchemaService.save(existing);
            result.put("success", true);
            result.put("data", updated);
        } else {
            result.put("success", false);
            result.put("message", "Form not found");
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        formSchemaService.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }
}
