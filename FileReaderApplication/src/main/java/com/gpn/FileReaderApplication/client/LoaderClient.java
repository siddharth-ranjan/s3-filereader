package com.gpn.FileReaderApplication.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class LoaderClient {
    private final WebClient webClient;

    public LoaderClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://loader-service:8081").build();
    }

    public String getNext(String filename) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/next")
                        .queryParam("filename", filename)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getCustom(String filename, int count) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/custom")
                        .queryParam("filename", filename)
                        .queryParam("count", count)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
