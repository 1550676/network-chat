package ru.zakharova.elena.network.chat.common.message;

public class PrivateMessage {

    public String from;
    public String to;
    public String message;

    public PrivateMessage(String from, String to, String message) {
        this.from = from;
        this.to = to;
        this.message = message;
    }

}
