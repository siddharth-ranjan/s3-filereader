package com.gpn.FileReaderApplication.controller;

import com.gpn.FileReaderApplication.dto.FileRequest;
import com.gpn.FileReaderApplication.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/records")
public class FileController {
    FileService fileService;

    public FileController(FileService service) {
        this.fileService = service;
    }

    @PostMapping("/next")
    public String getNext(HttpServletRequest request, @RequestBody FileRequest body) {
        String requestId = (String) request.getAttribute("X-Request-Id");

        return fileService.getNext(body.getFilename(), requestId);
    }

    @PostMapping("/custom")
    public String getCustom(HttpServletRequest request, @RequestBody FileRequest body) {
        String requestId = (String) request.getAttribute("X-Request-Id");

        return fileService.getCustom(body.getFilename(), body.getCount(), requestId);
    }
}
