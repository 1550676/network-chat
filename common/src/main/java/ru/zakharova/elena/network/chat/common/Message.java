package ru.zakharova.elena.network.chat.common;

import com.google.gson.Gson;
import ru.zakharova.elena.network.chat.common.message.*;

import java.util.List;

public class Message {

    public Command command;

    public PrivateMessage privateMessage;
    public AuthMessage authMessage;
    public AuthOkMessage authOkMessage;
    public AuthErrorMessage authErrorMessage;
    public PublicMessage publicMessage;
    public ClientListMessage clientListMessage;
    public NickChangeMessage nickChangeMessage;
    public NickChangeOkMessage nickChangeOkMessage;
    public NickChangeErrorMessage nickChangeErrorMessage;

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static Message fromJson(String json) {
        return new Gson().fromJson(json, Message.class);
    }

    private static Message create(Command cmd) {
        Message m = new Message();
        m.command = cmd;
        return m;
    }

    public static Message createClientList(List<String> nicknames) {
        Message message = create(Command.CLIENT_LIST);
        ClientListMessage msg = new ClientListMessage();
        msg.online = nicknames;
        message.clientListMessage = msg;
        return message;
    }

    public static Message createPublic(String from, String message) {
        Message m = create(Command.PUBLIC_MESSAGE);
        m.publicMessage = new PublicMessage(from, message);
        return m;
    }
    public static Message createPrivate(String from, String to, String message) {
        Message m = create(Command.PRIVATE_MESSAGE);
        m.privateMessage = new PrivateMessage(from, to, message);
        return m;
    }

    public static Message createPrivateFrom (String from, String to, String message) {
        Message m = create(Command.PRIVATE_MESSAGE_FROM);
        m.privateMessage = new PrivateMessage(from, to, message);
        return m;
    }

    public static Message createNickChange(String nick) {
        Message m = create(Command.NICK_CHANGE);
        m.nickChangeMessage = new NickChangeMessage(nick);
        return m;
    }
    public static Message createNickChangeOk() {
        Message m = create(Command.NICK_CHANGE_OK);
        return m;
    }
    public static Message createNickChangeError(String errorMsg) {
        Message m = create(Command.NICK_CHANGE_ERROR);
        m.nickChangeErrorMessage = new NickChangeErrorMessage(errorMsg);
        return m;
    }

    public static Message createAuth(String login, String password) {
        Message m = create(Command.AUTH_MESSAGE);
        m.authMessage = new AuthMessage(login, password);
        return m;
    }
    public static Message createAuthOk(String nickname) {
        Message m = create(Command.AUTH_OK);
        m.authOkMessage = new AuthOkMessage(nickname);
        return m;
    }
    public static Message createAuthError(String errorMsg) {
        Message m = create(Command.AUTH_ERROR);
        m.authErrorMessage = new AuthErrorMessage(errorMsg);
        return m;
    }

    public static Message serverEndMessage() {
        return create(Command.END);
    }

}
