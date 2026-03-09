package com.gpn.FileReaderApplication.service;

import com.gpn.FileReaderApplication.client.LoaderClient;
import org.springframework.stereotype.Service;

@Service
public class FileService {
    private final LoaderClient loaderClient;

    public FileService(LoaderClient loaderClient) {
        this.loaderClient = loaderClient;
    }

    public String getNext(String filename, String requestId) {
        return loaderClient.getNext(filename, requestId);
    }

    public String getCustom(String filename, int count, String requestId) {
        return loaderClient.getCustom(filename, count, requestId);
    }
}
