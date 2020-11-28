package ru.zakharova.elena.network.chat.common.message;

public class AuthMessage {

    public String login;
    public String password;

    public AuthMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }

}
