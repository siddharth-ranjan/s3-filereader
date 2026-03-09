package com.filereader.loader.controller;

import com.filereader.loader.redis.LoaderRedisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/getNext")
public class LoaderController {

    private final LoaderRedisService loaderRedisService;

    public LoaderController(LoaderRedisService loaderRedisService) {
        this.loaderRedisService = loaderRedisService;
    }

    @GetMapping()
    public ResponseEntity<?> getNext(
            @RequestParam String filename,
            @RequestParam(defaultValue = "1") int count) {

        try {
            List<String> records = loaderRedisService.getNextRecords(filename, count);
            if (records.isEmpty()) {
                return ResponseEntity.ok().body("EOF: No more records found.");
            }
            return ResponseEntity.ok(records);
        } catch (RuntimeException e) {
            return ResponseEntity.status(429).body(e.getMessage());
        }

    }
}