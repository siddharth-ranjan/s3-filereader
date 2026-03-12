package com.gpn.FileReaderApplication.dto;

public class FileRequest {
    private String filename;
    private int count;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
