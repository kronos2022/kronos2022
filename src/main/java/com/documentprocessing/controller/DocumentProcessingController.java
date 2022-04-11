package com.documentprocessing.controller;

import com.documentprocessing.models.OcrFileInput;
import com.documentprocessing.service.DocumentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
class DocumentProcessingController {

    @Autowired
    DocumentProcessingService documentProcessingService;

    @GetMapping(value = "/test")
    private String testApplication() {
        return "Spring boot application running";
    }

    @PostMapping(value = "/fullDocumentProcessing")
    private String fullDocumentProcessing(@RequestBody OcrFileInput ocrFileInput) throws IOException {
        System.out.println("File path" +ocrFileInput.getFilePath());
        documentProcessingService.fullDocumentProcessing(ocrFileInput.getFilePath());
        return "Success";
    }
}
