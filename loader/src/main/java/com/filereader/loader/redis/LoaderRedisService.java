package com.filereader.loader.redis;

import com.filereader.loader.s3Client.S3CsvReaderService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class LoaderRedisService {

    private final StringRedisTemplate redisTemplate;
    private final S3CsvReaderService s3CsvReaderService;
    private final RedissonClient redissonClient;

    private static final int CHUNK_SIZE = 20;

    public LoaderRedisService(StringRedisTemplate redisTemplate, S3CsvReaderService s3CsvReaderService, RedissonClient redissonClient) {
        this.redisTemplate = redisTemplate;
        this.s3CsvReaderService = s3CsvReaderService;
        this.redissonClient = redissonClient;
    }

    public List<String> getNextRecords(String filename, int requestedCount) {
        String cacheKey = "cache:" + filename;
        String lockKey = "lock:s3:" + filename;
        String counterKey = "counter:" + filename;

        long currentCacheSize = getListSize(cacheKey);

        if (currentCacheSize >= requestedCount) {               // cache hit
            return fetchFromCache(cacheKey, requestedCount);
        }

        RLock rLock = redissonClient.getLock(lockKey);
        boolean locked = false;

        try {
            // Wait up to 5 seconds to acquire the lock, auto-release after 30 seconds
            locked = rLock.tryLock(5, 10, TimeUnit.SECONDS);

            if (locked) {
                long cacheSize = getListSize(cacheKey);

                while (cacheSize < requestedCount) {
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

                    cacheSize = getListSize(cacheKey);
                }
            } else {
                // If lock was not acquired, another instance is probably loading data.
                // Optionally wait a short time and then proceed to fetch.
                Thread.sleep(500);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while acquiring lock", e);
        } finally {
            if (locked && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }

        return fetchFromCache(cacheKey, requestedCount);
    }

    private long getListSize(String key) {
        Long size = redisTemplate.opsForList().size(key);
        return size != null ? size : 0;
    }

    private List<String> fetchFromCache(String cacheKey, int count) {
        List<String> records = redisTemplate.opsForList().range(cacheKey, 0, count - 1);
        if (records != null && !records.isEmpty()) {
            redisTemplate.opsForList().trim(cacheKey, records.size(), -1);
        }
        return records;
    }
}

/*
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
 */