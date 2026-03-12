//package com.gpn.FileReaderApplication.client;
//
//import com.gpn.FileReaderApplication.dto.CardRecord;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Component
//public class LoaderClient {
//    private final WebClient webClient;
//
//    public LoaderClient(WebClient.Builder builder) {
//        this.webClient = builder.baseUrl("http://54.88.192.54:8080").build();
//    }
//
//    public CardRecord getNext(String filename, String requestId) {
//
//        try {
//
//            List<String> record = webClient.get()
//                    .uri(uriBuilder -> uriBuilder
//                            .path("/getNext")
//                            .queryParam("filename", filename)
//                            .build())
//                    .header("X-Request-Id", requestId)
//                    .retrieve()
//                    .onStatus(HttpStatusCode::isError, response ->
//                            response.bodyToMono(String.class)
//                                    .map(body -> new RuntimeException(
//                                            "Loader service error: " + body)))
//                    .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
//                    .block();
//
//            if (record == null || record.isEmpty()) {
//                throw new RuntimeException("Empty response from loader service");
//            }
//
//            String[] parts = record.getFirst().split(",");
//
//            if (parts.length != 3) {
//                throw new RuntimeException(
//                        "Invalid record format received from loader service: " + record);
//            }
//
//            return new CardRecord(parts[0].trim(), parts[1].trim(), parts[2].trim());
//
//        } catch (WebClientResponseException ex) {
//            throw new RuntimeException(
//                    "Failed to fetch record from loader service. requestId=" + requestId, ex);
//        }
//    }
//
//    public List<CardRecord> getCustom(String filename, int count, String requestId) {
//
//        try {
//
//            List<String> records = webClient.get()
//                    .uri(uriBuilder -> uriBuilder
//                            .path("/getNext")
//                            .queryParam("filename", filename)
//                            .queryParam("count", count)
//                            .build())
//                    .header("X-Request-Id", requestId)
//                    .retrieve()
//                    .onStatus(HttpStatusCode::isError, response ->
//                            response.bodyToMono(String.class)
//                                    .map(body -> new RuntimeException(
//                                            "Loader service error: " + body)))
//                    .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
//                    .block();
//
//            if (records == null || records.isEmpty()) {
//                throw new RuntimeException("Empty response received from loader service");
//            }
//
//            return records.stream()
//                    .map(record -> {
//
//                        if (record == null || record.isBlank()) {
//                            throw new RuntimeException("Invalid empty record received");
//                        }
//
//                        String[] parts = record.split(",");
//
//                        if (parts.length != 3) {
//                            throw new RuntimeException(
//                                    "Invalid record format received from loader service: " + record);
//                        }
//
//                        return new CardRecord(
//                                parts[0].trim(),
//                                parts[1].trim(),
//                                parts[2].trim()
//                        );
//
//                    })
//                    .toList();
//
//        } catch (WebClientResponseException ex) {
//            throw new RuntimeException(
//                    "Failed to fetch records from loader service. requestId=" + requestId, ex);
//        }
//    }
//}

package com.gpn.FileReaderApplication.client;

import com.gpn.FileReaderApplication.dto.CardRecord;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Component
public class LoaderClient {

    private final WebClient webClient;

    public LoaderClient(WebClient.Builder builder, @Value("${loader.service.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();

    }

    public List<CardRecord> getNext(String filename, Integer count, String requestId) {

        try {

            List<CardRecord> records = webClient.get()
                    .uri(uriBuilder -> {

                        uriBuilder.path("/getNext")
                                .queryParam("filename", filename);

                        if (count != null && count > 1) {
                            uriBuilder.queryParam("count", count);
                        }

                        return uriBuilder.build();
                    })
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
