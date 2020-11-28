package ru.zakharova.elena.network.chat.common;

public enum Command {

    PUBLIC_MESSAGE,
    PRIVATE_MESSAGE,
    PRIVATE_MESSAGE_FROM,
    AUTH_MESSAGE,
    AUTH_OK,
    AUTH_ERROR,
    END,
    CLIENT_LIST,
    NICK_CHANGE,
    NICK_CHANGE_OK,
    NICK_CHANGE_ERROR
}
