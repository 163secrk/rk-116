package com.formbuilder.repository;

import com.formbuilder.entity.FormSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormSchemaRepository extends JpaRepository<FormSchema, Long> {
    List<FormSchema> findByNameContainingIgnoreCase(String name);
}
