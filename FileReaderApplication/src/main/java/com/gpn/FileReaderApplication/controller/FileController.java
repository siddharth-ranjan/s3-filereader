package com.gpn.FileReaderApplication.controller;

import com.gpn.FileReaderApplication.service.FileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/records")
public class FileController {
    FileService fileService;

    public FileController(FileService service) {
        this.fileService = service;
    }

    @GetMapping
    public String getNext(@RequestParam String filename) {
        return fileService.getNext(filename);
    }

    @GetMapping("/custom")
    public String getCustom(@RequestParam String filename, @RequestParam int count) {
        return fileService.getCustom(filename, count);
    }
}
