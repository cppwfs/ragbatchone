package io.spring.ragbatchone;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Instruction {

    private String message;

    private Map<String,String> keyData = new  HashMap<String, String>();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getKeyData() {
        return keyData;
    }

    public void setKeyData(Map<String, String> keyData) {
        this.keyData = keyData;
    }
}
