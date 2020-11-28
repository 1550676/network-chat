package ru.zakharova.elena.network.chat.server.auth;

import java.util.List;

public class BaseAuthService implements AuthService{
    private static class Entry {
        private String login;
        private String password;
        private String nick;

        public Entry(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
    }

    private final List<Entry> entries = List.of(
            new Entry("l1", "p1", "nick1"),
            new Entry("l2", "p2", "nick2"),
            new Entry("l3", "p3", "nick3")
    );


    @Override
    public void start() {
        System.out.println("Auth service is running");
    }

    @Override
    public void stop() {
        System.out.println("Auth service has stopped");
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (Entry entry : entries) {
            if (entry.login.equals(login) && entry.password.equals(pass)) {
                return entry.nick;
            }
        }

        return null;
    }

    @Override
    public boolean isNickBusy(String newNick) {
        System.out.println("Заглушка");
        return true;
    }

    @Override
    public void changeNick(String nick, String newNick) {
        System.out.println("Заглушка");
    }
}

