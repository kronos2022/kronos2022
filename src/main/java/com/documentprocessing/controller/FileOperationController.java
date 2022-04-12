package com.documentprocessing.controller;

import com.documentprocessing.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/file-operations")
@Slf4j
@CrossOrigin(origins = "*")
@SuppressWarnings("unused")
public class FileOperationController {

    @Autowired
    private FileService fileService;

    @PostMapping(value = "/upload")
    public Object upload(@RequestPart MultipartFile multipartFile) throws IOException {
        log.info("HIT -/upload | File Name : {}", multipartFile.getOriginalFilename());
        return fileService.upload(multipartFile);
    }

    @PostMapping("/download")
    public Object download(@RequestParam String fileName) throws IOException {
        log.info("HIT -/download | File Name : {}", fileName);
        return fileService.download(fileName);
    }

    @GetMapping("/test")
    public void test() {
        fileService.someMethod();
    }
}
