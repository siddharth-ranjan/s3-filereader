//package com.gpn.FileReaderApplication.service;
//
//import com.gpn.FileReaderApplication.client.LoaderClient;
//import com.gpn.FileReaderApplication.dto.CardRecord;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class FileService {
//    private final LoaderClient loaderClient;
//
//    public FileService(LoaderClient loaderClient) {
//        this.loaderClient = loaderClient;
//    }
//
//    public CardRecord getNext(String filename, String requestId) {
//        return loaderClient.getNext(filename, requestId);
//    }
//
//    public List<CardRecord> getCustom(String filename, int count, String requestId) {
//        return loaderClient.getCustom(filename, count, requestId);
//    }
//}

package com.gpn.FileReaderApplication.service;

import com.gpn.FileReaderApplication.client.LoaderClient;
import com.gpn.FileReaderApplication.dto.CardRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileService {

    private final LoaderClient loaderClient;

    public FileService(LoaderClient loaderClient) {
        this.loaderClient = loaderClient;
    }

    public List<CardRecord> getNext(String filename, Integer count, String requestId) {
        return loaderClient.getNext(filename, count, requestId);
    }
}