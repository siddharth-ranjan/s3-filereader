package com.gpn.FileReaderApplication.dto;

public class CardRecord {
    private String cardNumber;
    private String network;
    private String type;

    public CardRecord(String cardNumber, String network, String type) {
        this.cardNumber = cardNumber;
        this.network = network;
        this.type = type;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getNetwork() {
        return network;
    }

    public String getType() {
        return type;
    }
}
