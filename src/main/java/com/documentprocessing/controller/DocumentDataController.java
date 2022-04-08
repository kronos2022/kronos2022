package com.documentprocessing.controller;

import com.documentprocessing.model.Person;
import com.documentprocessing.service.DocumentDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/document-data")
@Slf4j
@SuppressWarnings("unused")
public class DocumentDataController {

    @Autowired
    DocumentDataService documentDataService;

    @PostMapping("/save-data")
    private ResponseEntity<Object> saveDocument(@RequestBody final List<Person> persons) {
        documentDataService.saveDocument(persons);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-data")
    private ResponseEntity<List<Person>> getDocument(@RequestParam final String name,
                                                     @RequestParam final String designation) throws ExecutionException, InterruptedException {
        List<Person> persons = documentDataService.getDocumentsByNameAndStatus(name, designation);
        return ResponseEntity.ok().body(persons);
    }

    @GetMapping("/get-all-data")
    private ResponseEntity<List<Person>> getDocument() throws ExecutionException, InterruptedException {
        List<Person> persons = documentDataService.getAllDocuments();
        return ResponseEntity.ok().body(persons);
    }
}