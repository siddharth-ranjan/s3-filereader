package com.filereader.loader.s3Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3CsvReaderService {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.folder}")
    private String folder;

    public S3CsvReaderService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public List<String> readRowsFromS3(String filename, int startRow, int limit) {
        List<String> records = new ArrayList<>();
        filename = folder + "/" +    filename;
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(s3Client.getObject(request), StandardCharsets.UTF_8))) {

            for (int i = 0; i < startRow; i++) {
                if (reader.readLine() == null) {
                    return records; // Reached EOF during skip
                }
            }

            String line;
            while (records.size() < limit && (line = reader.readLine()) != null) {
                records.add(line);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading from S3: " + filename, e);
        }
        return records;
    }
}