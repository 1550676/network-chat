package ru.zakharova.elena.network.chat.server.auth;


import javax.annotation.Nullable;

public interface AuthService {
    void start();
    void stop();

    @Nullable
    String getNickByLoginPass(String login, String pass);

    boolean isNickBusy(String newNick);

    void changeNick(String nick, String newNick);
}
