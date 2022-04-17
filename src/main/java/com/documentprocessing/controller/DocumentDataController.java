package com.documentprocessing.controller;

import com.documentprocessing.service.DocumentDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/document-data")
@Slf4j
@SuppressWarnings("unused")
public class DocumentDataController {

    @Autowired
    DocumentDataService documentDataService;
}