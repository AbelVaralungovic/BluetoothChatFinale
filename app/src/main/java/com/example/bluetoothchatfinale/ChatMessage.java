package com.example.bluetoothchatfinale;

import java.util.ArrayList;

public class ChatMessage extends ArrayList {
    private boolean left;
    private String message;


    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ChatMessage(boolean left, String message) {
        this.left = left;
        this.message = message;

    }
}
