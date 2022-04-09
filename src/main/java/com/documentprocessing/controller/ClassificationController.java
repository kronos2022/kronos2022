package com.documentprocessing.controller;

import com.documentprocessing.service.ClassificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
class ClassificationController {

    @Autowired
    private ClassificationService classificationService;

    @Value("${project-id}")
    private String projectId;
    @Value("${classification.file-path}")
    private String filePath;
    @Value("${classification.location}")
    private String location;
    @Value("${classification.processor-id}")
    private String processorId;
    @Value("${classification.model-id}")
    private String modelId;

    @PostMapping(value = "/classifyDocument")
    private String classifyDocument() throws IOException {
        classificationService.classifyDocument(projectId, location, processorId, filePath, modelId);
        return "Success";
    }
}
