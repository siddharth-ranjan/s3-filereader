package com.filereader.loader.controller;

import com.filereader.loader.redis.LoaderRedisService;
import com.filereader.loader.s3Client.S3CsvReaderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/getNext")
public class LoaderController {

    private final LoaderRedisService loaderRedisService;
    private final S3CsvReaderService s3CsvReaderService;

    public LoaderController(LoaderRedisService loaderRedisService,  S3CsvReaderService s3CsvReaderService) {
        this.loaderRedisService = loaderRedisService;
        this.s3CsvReaderService = s3CsvReaderService;
    }

    @GetMapping()
    public ResponseEntity<?> getNext(
            @RequestParam String filename,
            @RequestParam(defaultValue = "1") int count) {

//        System.out.println(s3CsvReaderService.readRowsFromS3(filename, 0, count));
//        return "Success";
        try {
            List<String> records = loaderRedisService.getNextRecords(filename, count);
            if (records.isEmpty()) {
                return ResponseEntity.ok().body("EOF: No more records found.");
            }
            return ResponseEntity.ok(records);
        } catch (RuntimeException e) {
            return ResponseEntity.status(429).body(e.getMessage()); // 429 Too Many Requests or 503
        }

    }
}