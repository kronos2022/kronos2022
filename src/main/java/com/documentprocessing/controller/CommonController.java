package com.documentprocessing.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/")
@SuppressWarnings("unused")
public class CommonController {

    @GetMapping(value = "test-app")
    private String testApplication() {
        return "Spring boot application running";
    }

}
