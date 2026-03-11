package com.filereader.loader.model;

public class AccountRecord {

    private String accountNumber;
    private String accountType;
    private String status;

    public AccountRecord(String accountNumber, String accountType, String status) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.status = status;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getAccountType() { return accountType; }
    public String getStatus() { return status; }

    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public void setStatus(String status) { this.status = status; }
}