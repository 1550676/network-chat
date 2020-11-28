package ru.zakharova.elena.network.chat.server;

import org.apache.log4j.Logger;
import ru.zakharova.elena.network.chat.common.Command;
import ru.zakharova.elena.network.chat.common.Message;
import ru.zakharova.elena.network.chat.common.message.AuthMessage;
import ru.zakharova.elena.network.chat.common.message.NickChangeMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {
    private final static Logger adminLogger = Logger.getLogger(ClientHandler.class);

    private MyServer myServer;
    private String clientName;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public ClientHandler(Socket socket, MyServer myServer) {
        try {
            this.socket = socket;
            this.myServer = myServer;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    authentication();
                    readMessages();
                } catch (IOException e) {
                    adminLogger.error("Connection was lost. " + e.toString());
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            adminLogger.error("Failed to create client handler. " + e.toString());
        }

    }

    private void readMessages() throws IOException {
        while (true) {
            String clientMessage = in.readUTF();
            adminLogger.debug("Command from client was received: " + clientMessage);
            Message m = Message.fromJson(clientMessage);
            switch (m.command) {
                case PUBLIC_MESSAGE:
                    myServer.broadcastMessage(m);
                    adminLogger.info("Command from client was executed: " + clientMessage);
                    break;
                case PRIVATE_MESSAGE:
                    myServer.sendPrivateMessage(m);
                    adminLogger.info("Command from client was executed: " + clientMessage);
                    break;
                case END:
                    adminLogger.info("Client has been disconnected.");
                    return;
                case NICK_CHANGE: {
                    NickChangeMessage nickChangeMessage = m.nickChangeMessage;
                    String newNick = nickChangeMessage.nickname;
                    if (myServer.getAuthService().isNickBusy(newNick)) {
                        sendMessage(Message.createNickChangeError("This nickname is already busy, try to enter another."));
                        adminLogger.info("Failed change of nickname. Nickname '" + newNick + "' is already busy.");
                    } else {
                        adminLogger.info("Successful change of nickname. " +
                                "Nickname '" + clientName + "' was changed to '" + newNick + "'.");
                        myServer.getAuthService().changeNick(clientName, newNick);
                        sendMessage(Message.createNickChangeOk());
                        myServer.broadcastMessage(Message.createPublic(null, clientName + " сменил ник на: " + newNick));
                        clientName = newNick;
                        myServer.updateSubscribeAfterNickChange();
                    }
                }
            }
        }
    }

    private void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcastMessage(Message.createPublic(null, clientName + " is offline"));
        try {
            socket.close();
        } catch (IOException e) {
            adminLogger.error("Failed to close socket! " + e.toString());
        }
    }

    // "/auth login password"
    private void authentication() throws IOException {
        Timer myTimer = new Timer();
        TimerTask myTimerTask = new TimerTask() {
            @Override
            public void run() {
                sendMessage(Message.createAuthError("The connection time has out."));
                adminLogger.debug("Failed authentication. The connection time has out. " +
                        "Socket will be closed.");
                try {
                    socket.close();
                } catch (IOException e) {
                    adminLogger.error("Failed to close socket! " + e.toString());
                }
            }
        };
        myTimer.schedule(myTimerTask, 120*1000);

        while (true){
            String clientMessage = in.readUTF();
            Message message = Message.fromJson(clientMessage);
        if (message.command == Command.AUTH_MESSAGE) {
            AuthMessage authMessage = message.authMessage;
            String login = authMessage.login;
            String password = authMessage.password;
            String nick = myServer.getAuthService().getNickByLoginPass(login, password);
            if (nick == null) {
                sendMessage(Message.createAuthError("Invalid login or password"));
                adminLogger.info("Failed authentication. Invalid login '" + login +
                        "' or password '" + password + "'.");
                continue;
            }
            if (myServer.isNickBusy(nick)) {
                sendMessage(Message.createAuthError("The account is already in use."));
                adminLogger.info("Failed authentication. The account with login '" + login +
                        "' and password '" + password + "' is already in use.");
                continue;
            }
            adminLogger.info("Successful authentication. Login '" + login + "', password '" + password + "'.");
            clientName = nick;
            sendMessage(Message.createAuthOk(clientName));
            myServer.broadcastMessage(Message.createPublic(null, clientName + " is online"));
            myServer.subscribe(this);
            myTimer.cancel();
            break;
        }}
    }

    private void sendMessage(Message message) {
        sendMessage(message.toJson());
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            adminLogger.error("Failed to send message " + message + ". " + e.toString());
        }
    }

    public String getClientName() {
        return clientName;
    }

}