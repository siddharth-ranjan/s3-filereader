package com.filereader.loader.redis;

import com.filereader.loader.dto.CardRecord;
import com.filereader.loader.s3Client.S3CsvReaderService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoaderRedisService {

    private final StringRedisTemplate redisTemplate;
    private final S3CsvReaderService s3CsvReaderService;
    private static final int CHUNK_SIZE = 20;

    public LoaderRedisService(StringRedisTemplate redisTemplate, S3CsvReaderService s3CsvReaderService) {
        this.redisTemplate = redisTemplate;
        this.s3CsvReaderService = s3CsvReaderService;
    }

    public List<CardRecord> getNextRecords(String filename, int requestedCount) {
        String cacheKey = "cache:" + filename;
        String lockKey = "lock:s3:" + filename;
        String counterKey = "counter:" + filename;

        long currentCacheSize = redisTemplate.opsForList().size(cacheKey) != null ?
                redisTemplate.opsForList().size(cacheKey) : 0;

        if (currentCacheSize >= requestedCount) {
            return fetchFromCache(cacheKey, requestedCount);
        }

        int maxRetries = 10;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            boolean isLocked = Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(10))
            );

            if (isLocked) {
                try {
                    long doubleCheckSize = redisTemplate.opsForList().size(cacheKey) != null ?
                            redisTemplate.opsForList().size(cacheKey) : 0;

                    while (doubleCheckSize < requestedCount) {
                        String currentCounterStr = redisTemplate.opsForValue().get(counterKey);
                        int startRow = (currentCounterStr == null) ? 1 : Integer.parseInt(currentCounterStr);

                        List<String> newRecords = s3CsvReaderService.readRowsFromS3(filename, startRow, CHUNK_SIZE);

                        if (!newRecords.isEmpty()) {
                            redisTemplate.opsForList().rightPushAll(cacheKey, newRecords);
                            redisTemplate.opsForValue().set(counterKey, String.valueOf(startRow + newRecords.size()));
                        }

                        if (newRecords.size() < CHUNK_SIZE) {
                            redisTemplate.opsForValue().set(counterKey, "1");
                            break;
                        }

                        doubleCheckSize = redisTemplate.opsForList().size(cacheKey) != null ?
                                redisTemplate.opsForList().size(cacheKey) : 0;
                    }
                } finally {
                    redisTemplate.delete(lockKey);
                }
                break;
            } else {
                // Wait and retry
                System.out.println("Waiting! Try: " + retryCount);
                retryCount++;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for lock", e);
                }

                long refreshedSize = redisTemplate.opsForList().size(cacheKey) != null ?
                        redisTemplate.opsForList().size(cacheKey) : 0;
                if (refreshedSize >= requestedCount) {
                    break;
                }
            }
        }

        return fetchFromCache(cacheKey, requestedCount);
    }

    private List<CardRecord> fetchFromCache(String cacheKey, int count) {
        List<String> records = redisTemplate.opsForList().range(cacheKey, 0, count - 1);
        List<CardRecord> dtoList=new ArrayList<>();

        if (records != null && !records.isEmpty()) {
            for(String r:records){
                dtoList.add(mapToDto(r));
            }

        }
        return dtoList;
    }
    private CardRecord mapToDto(String record){
        String[] parts = record.split(",");

        return new CardRecord(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim()
        );
    }
}