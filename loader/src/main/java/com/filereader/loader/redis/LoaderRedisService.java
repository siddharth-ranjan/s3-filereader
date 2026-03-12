package com.filereader.loader.redis;

import com.filereader.loader.model.AccountRecord;
import com.filereader.loader.s3Client.S3CsvReaderService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class LoaderRedisService {

    private final StringRedisTemplate redisTemplate;
    private final S3CsvReaderService s3CsvReaderService;
    private final RedissonClient redissonClient;
    private static final int CHUNK_SIZE = 20;

    public LoaderRedisService(StringRedisTemplate redisTemplate,
                              S3CsvReaderService s3CsvReaderService,
                              RedissonClient redissonClient) {
        this.redisTemplate = redisTemplate;
        this.s3CsvReaderService = s3CsvReaderService;
        this.redissonClient = redissonClient;
    }

    public List<AccountRecord> getNextRecords(String filename, int requestedCount) {
        String cacheKey = "cache:" + filename;
        String counterKey = "counter:" + filename;
        String lockKey = "lock:s3:" + filename;

        long currentCacheSize = redisTemplate.opsForList().size(cacheKey) != null ?
                redisTemplate.opsForList().size(cacheKey) : 0;

        if (currentCacheSize >= requestedCount) {
            return parseRecords(fetchFromCache(cacheKey, requestedCount));
        }

        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(5, -1, TimeUnit.SECONDS);

            if (isLocked) {
                try {

                    long doubleCheckSize = redisTemplate.opsForList().size(cacheKey) != null ?
                            redisTemplate.opsForList().size(cacheKey) : 0;

                    while (doubleCheckSize < requestedCount) {
                        String currentCounterStr = redisTemplate.opsForValue().get(counterKey);
                        int startRow = (currentCounterStr == null) ? 1 : Integer.parseInt(currentCounterStr);

                        long remaining = requestedCount - doubleCheckSize;
                        int fetchSize = ((int) ((remaining + CHUNK_SIZE - 1) / CHUNK_SIZE)) * CHUNK_SIZE;

                        List<String> newRecords = s3CsvReaderService.readRowsFromS3(filename, startRow, fetchSize);

                        if (!newRecords.isEmpty()) {
                            redisTemplate.opsForList().rightPushAll(cacheKey, newRecords);
                            redisTemplate.opsForValue().set(counterKey, String.valueOf(startRow + newRecords.size()));
                        }

                        if (newRecords.size() < fetchSize) {
                            // EOF reached
                                if (newRecords.isEmpty() && redisTemplate.opsForList().size(cacheKey) == 0) {
                                // Counter was already at EOF, nothing cached yet — reset and retry from start
                                redisTemplate.opsForValue().set(counterKey, "1");
//                                eofResetDone = true;
                                continue;
                            }
                            // EOF with some records fetched — reset counter, return what we have
                            redisTemplate.opsForValue().set(counterKey, "1");
                            long available = redisTemplate.opsForList().size(cacheKey) != null ?
                                    redisTemplate.opsForList().size(cacheKey) : 0;
                            int returnCount = (int) Math.min(available, requestedCount);
                            return parseRecords(fetchFromCache(cacheKey, returnCount));
                        }

                        doubleCheckSize = redisTemplate.opsForList().size(cacheKey) != null ?
                                redisTemplate.opsForList().size(cacheKey) : 0;
                    }
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                throw new RuntimeException("Could not acquire lock after 5 seconds. Please retry shortly.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for lock", e);
        }

        return parseRecords(fetchFromCache(cacheKey, requestedCount));
    }

    private List<String> fetchFromCache(String cacheKey, int count) {
        List<String> records = redisTemplate.opsForList().range(cacheKey, 0, count - 1);
        if (records != null && !records.isEmpty()) {
            redisTemplate.opsForList().trim(cacheKey, records.size(), -1);
        }
        return records;
    }

    private List<AccountRecord> parseRecords(List<String> rawRecords) {
        List<AccountRecord> result = new ArrayList<>();
        if (rawRecords == null) return result;
        for (String line : rawRecords) {
            String[] parts = line.split(",", 3);
            String accountNumber = parts.length > 0 ? parts[0].trim() : "";
            String accountType = parts.length > 1 ? parts[1].trim() : "";
            String status = parts.length > 2 ? parts[2].trim() : "";
            result.add(new AccountRecord(accountNumber, accountType, status));
        }
        return result;
    }
}