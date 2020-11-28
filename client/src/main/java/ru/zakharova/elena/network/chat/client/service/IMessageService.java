package ru.zakharova.elena.network.chat.client.service;

import ru.zakharova.elena.network.chat.common.Message;

import java.io.Closeable;
import java.io.IOException;

public interface IMessageService extends Closeable {

    void sendMessage(Message message);

    void processRetrievedMessage(Message message);

    @Override
    default void close() throws IOException {

    }
}
