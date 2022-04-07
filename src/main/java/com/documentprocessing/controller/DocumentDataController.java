package com.documentprocessing.controller;

import com.documentprocessing.model.Person;
import com.documentprocessing.service.DocumentDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/document-data")
@Slf4j
public class DocumentDataController {

    @Autowired
    DocumentDataService documentDataService;

    @PostMapping("/save-data")
    private ResponseEntity<Object> saveDocument(@RequestBody final List<Person> persons) {
        documentDataService.saveDocument(persons);
        return ResponseEntity.ok().build();
    }
}