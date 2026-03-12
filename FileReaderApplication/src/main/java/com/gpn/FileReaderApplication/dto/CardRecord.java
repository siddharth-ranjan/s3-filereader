package com.gpn.FileReaderApplication.dto;

public class CardRecord {
    private String accountNumber;
    private String accountType;
    private String status;

    public CardRecord(String accountNumber, String accountType, String status) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.status = status;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getStatus() {
        return status;
    }
}
