package com.documentprocessing.controller;

import com.documentprocessing.models.OcrFileInput;
import com.documentprocessing.service.DocumentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
class DocumentProcessingController {

    @Autowired
    DocumentProcessingService documentProcessingService;


    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping(value = "/fullDocumentProcessing",headers="Accept=application/json")
    private String fullDocumentProcessing(@RequestPart MultipartFile multipartFile) throws IOException {
//        System.out.println("File path" +ocrFileInput.getFilePath());
        return documentProcessingService.fullDocumentProcessing(multipartFile);
    }
}
