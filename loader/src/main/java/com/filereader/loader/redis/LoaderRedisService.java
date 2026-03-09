package com.filereader.loader.redis;

import com.filereader.loader.s3Client.S3CsvReaderService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
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

    public List<String> getNextRecords(String filename, int requestedCount) {
        String cacheKey = "cache:" + filename;
        String lockKey = "lock:s3:" + filename;
        String counterKey = "counter:" + filename;

        long currentCacheSize = redisTemplate.opsForList().size(cacheKey) != null ?
                redisTemplate.opsForList().size(cacheKey) : 0;

        if (currentCacheSize >= requestedCount) {
            return fetchFromCache(cacheKey, requestedCount);
        }

        boolean isLocked = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(10))
        );

        if (isLocked) {
            try {
                long doubleCheckSize = redisTemplate.opsForList().size(cacheKey) != null ?
                        redisTemplate.opsForList().size(cacheKey) : 0;

                if (doubleCheckSize < requestedCount) {
                    String currentCounterStr = redisTemplate.opsForValue().get(counterKey);
                    int startRow = (currentCounterStr == null) ? 0 : Integer.parseInt(currentCounterStr);

                    List<String> newRecords = s3CsvReaderService.readRowsFromS3(filename, startRow, CHUNK_SIZE);

                    if (!newRecords.isEmpty()) {
                        redisTemplate.opsForList().rightPushAll(cacheKey, newRecords);
                        redisTemplate.opsForValue().set(counterKey, String.valueOf(startRow + newRecords.size()));
                    }
                }
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            throw new RuntimeException("Fetch in progress by another instance. Please retry shortly.");
        }

        return fetchFromCache(cacheKey, requestedCount);
    }

    private List<String> fetchFromCache(String cacheKey, int count) {
        List<String> records = redisTemplate.opsForList().range(cacheKey, 0, count - 1);
        if (records != null && !records.isEmpty()) {
            redisTemplate.opsForList().trim(cacheKey, records.size(), -1);
        }
        return records;
    }
}