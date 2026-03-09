package com.gpn.FileReaderApplication.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class LoaderClient {
    private final WebClient webClient;

    public LoaderClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://54.88.192.54:8080").build();
    }

    public String getNext(String filename, String requestId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getNext")
                        .queryParam("filename", filename)
                        .build())
                .header("X-Request-Id", requestId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getCustom(String filename, int count, String requestId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getNext")
                        .queryParam("filename", filename)
                        .queryParam("count", count)
                        .build())
                .header("X-Request-Id", requestId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
