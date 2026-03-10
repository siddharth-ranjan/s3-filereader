package com.filereader.loader.dto;

public class CardRecord {
    private String accountNumber;
    private String accountType;
    private String status;

    public CardRecord() {}

    public CardRecord(String accountNumber, String accountType, String status) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.status = status;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
