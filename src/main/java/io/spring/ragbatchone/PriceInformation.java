package io.spring.ragbatchone;

import java.util.HashMap;
import java.util.Map;

public class PriceInformation {

    private Double interest;
    private String message;

    private String myData;

    private Map<String,String> keyData = new HashMap<>();

    public PriceInformation() {
    }

    public PriceInformation(Double interest, String message, String myData) {
        this.interest = interest;
        this.message = message;
        this.myData = myData;
    }

    public Double getInterest() {
        return interest;
    }

    public void setInterest(Double interest) {
        this.interest = interest;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMyData() {
        return myData;
    }

    public void setMyData(String myData) {
        this.myData = myData;
    }

    public Map<String, String> getKeyData() {
        return keyData;
    }

    public void setKeyData(Map<String, String> keyData) {
        this.keyData = keyData;
    }
}
