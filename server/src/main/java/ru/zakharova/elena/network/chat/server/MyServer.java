package ru.zakharova.elena.network.chat.server;

import org.apache.log4j.Logger;
import ru.zakharova.elena.network.chat.common.Message;
import ru.zakharova.elena.network.chat.server.auth.AuthService;
import ru.zakharova.elena.network.chat.server.auth.DBAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    private final static Logger adminLogger = Logger.getLogger(MyServer.class);

    private static final int PORT = 8189;
    private final AuthService authService = new DBAuthService();
    private List<ClientHandler> currentClients = new ArrayList<>();

    public MyServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            authService.start();
            while (true) {
                adminLogger.info("Awaiting client connection...");
                Socket socket = serverSocket.accept();
                adminLogger.info("Client has connected");
                new ClientHandler(socket, this);
            }

        } catch (IOException e) {
            adminLogger.fatal("Problems with ServerSocket. " + e.toString());
        } finally {
            authService.stop();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        currentClients.add(clientHandler);
        broadcastClientsList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        currentClients.remove(clientHandler);
        broadcastClientsList();
    }

    public synchronized void updateSubscribeAfterNickChange() {
        broadcastClientsList();
    }

    private void broadcastClientsList() {
        List<String> nicknames = new ArrayList<>();
        for (ClientHandler client : currentClients) {
            nicknames.add(client.getClientName());
        }
        Message message = Message.createClientList(nicknames);
        broadcastMessage(message.toJson());
        adminLogger.info("List of current clients was refreshed: " + nicknames);
    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler client : currentClients) {
            if (client.getClientName().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastMessage(Message message) {
        broadcastMessage(message.toJson());
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler client : currentClients) {
            client.sendMessage(message);
        }
    }
    public synchronized void sendPrivateMessage (Message message) {
        for (ClientHandler client : currentClients) {
            if (client.getClientName().equals(message.privateMessage.to))
                client.sendMessage(message.toJson());
            if (client.getClientName().equals(message.privateMessage.from)) {
                Message msg = Message.createPrivateFrom(message.privateMessage.from, message.privateMessage.to, message.privateMessage.message);
                client.sendMessage(msg.toJson());
            }
        }
    }

}

