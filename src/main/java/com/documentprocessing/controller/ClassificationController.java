package com.documentprocessing.controller;

import com.documentprocessing.service.ClassificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
class ClassificationController {

    @Autowired
    ClassificationService classificationService;

    @PostMapping(value = "/classifyDocument")
    private String classifyDocument() throws IOException {
        String filePath="C://Users//Komal//Downloads//dtb_claim_form_insurance.pdf";
        classificationService.classifyDocument("psyched-signal-345109", "us", "60c96e1afcf82c4a", filePath, "TCN1923385036317392896");
        return "Success";
    }
}
