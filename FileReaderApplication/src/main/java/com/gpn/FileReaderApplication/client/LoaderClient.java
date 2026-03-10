package com.gpn.FileReaderApplication.client;

import com.gpn.FileReaderApplication.dto.CardRecord;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
public class LoaderClient {
    private final WebClient webClient;

    public LoaderClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://54.88.192.54:8080").build();

    }

    public CardRecord getNext(String filename, String requestId) {

        try {

            List<CardRecord> record = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/getNext")
                            .queryParam("filename", filename)
                            .build())
                    .header("X-Request-Id", requestId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .map(body -> new RuntimeException(
                                            "Loader service error: " + body)))
                    .bodyToMono(new ParameterizedTypeReference<List<CardRecord>>() {})
                    .block();

            if (record == null || record.isEmpty()) {
                throw new RuntimeException("Empty response from loader service");
            }
            return record.getFirst();

        } catch (WebClientResponseException ex) {
            throw new RuntimeException(
                    "Failed to fetch record from loader service. requestId=" + requestId, ex);
        }
    }

    public List<CardRecord> getCustom(String filename, int count, String requestId) {

        try {

            List<CardRecord> records = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/getNext")
                            .queryParam("filename", filename)
                            .queryParam("count", count)
                            .build())
                    .header("X-Request-Id", requestId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .map(body -> new RuntimeException(
                                            "Loader service error: " + body)))
                    .bodyToMono(new ParameterizedTypeReference<List<CardRecord>>() {})
                    .block();

            if (records == null || records.isEmpty()) {
                throw new RuntimeException("Empty response received from loader service");
            }
            return records;

        } catch (WebClientResponseException ex) {
            throw new RuntimeException(
                    "Failed to fetch records from loader service. requestId=" + requestId, ex);
        }
    }
}
