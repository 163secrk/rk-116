package com.formbuilder.service;

import com.formbuilder.entity.FormSchema;
import com.formbuilder.repository.FormSchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FormSchemaService {

    @Autowired
    private FormSchemaRepository formSchemaRepository;

    public List<FormSchema> findAll() {
        return formSchemaRepository.findAll();
    }

    public Optional<FormSchema> findById(Long id) {
        return formSchemaRepository.findById(id);
    }

    public FormSchema save(FormSchema formSchema) {
        return formSchemaRepository.save(formSchema);
    }

    public void deleteById(Long id) {
        formSchemaRepository.deleteById(id);
    }
}
