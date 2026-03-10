//////package com.gpn.FileReaderApplication.controller;
//////
//////import com.gpn.FileReaderApplication.dto.CardRecord;
//////import com.gpn.FileReaderApplication.dto.FileRequest;
//////import com.gpn.FileReaderApplication.service.FileService;
//////import jakarta.servlet.http.HttpServletRequest;
//////import org.springframework.web.bind.annotation.*;
//////
//////import java.util.List;
//////
//////@RestController
//////@RequestMapping("/records")
//////public class FileController {
//////    FileService fileService;
//////
//////    public FileController(FileService service) {
//////        this.fileService = service;
//////    }
//////
//////    @PostMapping("/next")
//////    public CardRecord getNext(HttpServletRequest request, @RequestBody FileRequest body) {
//////        String requestId = (String) request.getAttribute("X-Request-Id");
//////
//////        return fileService.getNext(body.getFilename(), requestId);
//////    }
//////
//////    @PostMapping("/custom")
//////    public List<CardRecord> getCustom(HttpServletRequest request, @RequestBody FileRequest body) {
//////        String requestId = (String) request.getAttribute("X-Request-Id");
//////
//////        return fileService.getCustom(body.getFilename(), body.getCount(), requestId);
//////    }
//////}
////
////
////package com.gpn.FileReaderApplication.controller;
////
////import com.gpn.FileReaderApplication.dto.CardRecord;
////import com.gpn.FileReaderApplication.dto.FileRequest;
////import com.gpn.FileReaderApplication.service.FileService;
////import jakarta.servlet.http.HttpServletRequest;
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////import org.springframework.web.bind.annotation.*;
////
////import java.util.List;
////
////@RestController
////@RequestMapping("/records")
////public class FileController {
////
////    private static final Logger log = LoggerFactory.getLogger(FileController.class);
////
////    private final FileService fileService;
////
////    public FileController(FileService service) {
////        this.fileService = service;
////    }
////
////    @PostMapping("/next")
////    public CardRecord getNext(HttpServletRequest request, @RequestBody FileRequest body) {
////
////        String requestId = (String) request.getAttribute("X-Request-Id");
////
////        log.info("Request received to fetch NEXT record for file: {} with requestId: {}",
////                body.getFilename(), requestId);
////
////        CardRecord record = fileService.getNext(body.getFilename(), requestId);
////
////        log.info("Returning record for file: {} with requestId: {}", body.getFilename(), requestId);
////
////        return record;
////    }
////
////    @PostMapping("/custom")
////    public List<CardRecord> getCustom(HttpServletRequest request, @RequestBody FileRequest body) {
////
////        String requestId = (String) request.getAttribute("X-Request-Id");
////
////        log.info("Request received to fetch {} records from file: {} with requestId: {}",
////                body.getCount(), body.getFilename(), requestId);
////
////        List<CardRecord> records = fileService.getCustom(body.getFilename(), body.getCount(), requestId);
////
////        log.info("Returning {} records for file: {} with requestId: {}",
////                records != null ? records.size() : 0, body.getFilename(), requestId);
////
////        return records;
////    }
////}
//
//
//package com.gpn.FileReaderApplication.controller;
//
//import com.gpn.FileReaderApplication.dto.CardRecord;
//import com.gpn.FileReaderApplication.dto.FileRequest;
//import com.gpn.FileReaderApplication.service.FileService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.bind.annotation.*;
//import io.micrometer.observation.annotation.Observed;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/records")
//public class FileController {
//
//    private static final Logger log = LoggerFactory.getLogger(FileController.class);
//
//    private final FileService fileService;
//
//    public FileController(FileService service) {
//        this.fileService = service;
//    }
//
//    @Observed(name = "get-next-record")
//    @PostMapping("/next")
//    public CardRecord getNext(HttpServletRequest request, @RequestBody FileRequest body) {
//
//        String requestId = (String) request.getAttribute("X-Request-Id");
//
//        log.info("Request received to fetch NEXT record for file: {} with requestId: {}",
//                body.getFilename(), requestId);
//
//        CardRecord record = fileService.getNext(body.getFilename(), requestId);
//
//        log.info("Returning record for file: {} with requestId: {}", body.getFilename(), requestId);
//
//        return record;
//    }
//
//    @Observed(name = "get-custom-records")
//    @PostMapping("/custom")
//    public List<CardRecord> getCustom(HttpServletRequest request, @RequestBody FileRequest body) {
//
//        String requestId = (String) request.getAttribute("X-Request-Id");
//
//        log.info("Request received to fetch {} records from file: {} with requestId: {}",
//                body.getCount(), body.getFilename(), requestId);
//
//        List<CardRecord> records = fileService.getCustom(body.getFilename(), body.getCount(), requestId);
//
//        log.info("Returning {} records for file: {} with requestId: {}",
//                records != null ? records.size() : 0, body.getFilename(), requestId);
//
//        return records;
//    }
//}


package com.gpn.FileReaderApplication.controller;

import com.gpn.FileReaderApplication.dto.CardRecord;
import com.gpn.FileReaderApplication.dto.FileRequest;
import com.gpn.FileReaderApplication.service.FileService;
import jakarta.servlet.http.HttpServletRequest;

import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/records")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;
    private final Tracer tracer;

    public FileController(FileService service, Tracer tracer) {
        this.fileService = service;
        this.tracer = tracer;
    }

    @Observed(name = "get-next-record")
    @PostMapping("/next")
    public CardRecord getNext(HttpServletRequest request, @RequestBody FileRequest body) {

        String requestId = (String) request.getAttribute("X-Request-Id");

        // Add UUID to Zipkin trace
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag("requestId", requestId);
            tracer.currentSpan().tag("filename", body.getFilename());
        }

        log.info("Request received to fetch NEXT record for file: {} with requestId: {}",
                body.getFilename(), requestId);

        CardRecord record = fileService.getNext(body.getFilename(), requestId);

        log.info("Returning record for file: {} with requestId: {}", body.getFilename(), requestId);

        return record;
    }

    @Observed(name = "get-custom-records")
    @PostMapping("/custom")
    public List<CardRecord> getCustom(HttpServletRequest request, @RequestBody FileRequest body) {

        String requestId = (String) request.getAttribute("X-Request-Id");

        // Add UUID to Zipkin trace
        if (tracer.currentSpan() != null) {
            tracer.currentSpan().tag("requestId", requestId);
            tracer.currentSpan().tag("filename", body.getFilename());
        }

        log.info("Request received to fetch {} records from file: {} with requestId: {}",
                body.getCount(), body.getFilename(), requestId);

        List<CardRecord> records = fileService.getCustom(body.getFilename(), body.getCount(), requestId);

        log.info("Returning {} records for file: {} with requestId: {}",
                records != null ? records.size() : 0, body.getFilename(), requestId);

        return records;
    }
}