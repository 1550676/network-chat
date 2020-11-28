package ru.zakharova.elena.network.chat.client.service;

public class ServerConnectionException extends RuntimeException {

    public ServerConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
