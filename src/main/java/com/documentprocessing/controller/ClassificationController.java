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
    private String classifyDocument(@RequestBody String filePath) throws IOException {
        classificationService.classifyDocument("psyched-signal-345109", "us", "", filePath, "TCN1923385036317392896");
        return "Success";
    }
}
