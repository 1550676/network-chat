package ru.zakharova.elena.network.chat.client.service;

import javafx.scene.control.TextArea;
import ru.zakharova.elena.network.chat.client.Network;
import ru.zakharova.elena.network.chat.client.PrimaryController;
import ru.zakharova.elena.network.chat.common.Message;
import ru.zakharova.elena.network.chat.common.message.PrivateMessage;
import ru.zakharova.elena.network.chat.common.message.PublicMessage;


import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class ServerMessageService implements IMessageService {

    private static final String HOST_ADDRESS_PROP = "server.address";
    private static final String HOST_PORT_PROP = "server.port";
    public static final String STOP_SERVER_COMMAND = "/end";
    public static final int NUMBER_LINES_HISTORY = 10;

    private String hostAddress;
    private int hostPort;

    private final TextArea chatTextArea;
    private PrimaryController primaryController;
    private boolean needStopServerOnClosed;
    private Network network;

    File file;

    public ServerMessageService(PrimaryController primaryController, boolean needStopServerOnClosed) {
        this.chatTextArea = primaryController.chatTextArea;
        this.primaryController = primaryController;
        this.needStopServerOnClosed = needStopServerOnClosed;
        initialize();
    }

    private void initialize() {
        readProperties();
        startConnectionToServer();
    }

    private void startConnectionToServer() {
        try {
            this.network = new Network(hostAddress, hostPort, this);
        } catch (IOException e) {
            throw new ServerConnectionException("Failed to connect to server", e);
        }
    }

    private void readProperties() {
        Properties serverProperties = new Properties();
        try (InputStream inputStream = getClass().getResourceAsStream("/application.properties")) {
            serverProperties.load(inputStream);
            hostAddress = serverProperties.getProperty(HOST_ADDRESS_PROP);
            hostPort = Integer.parseInt(serverProperties.getProperty(HOST_PORT_PROP));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read application.properties file", e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port value", e);
        }
    }

    @Override
    public void sendMessage(Message message) {
        network.send(message.toJson());
    }

    @Override
    public void processRetrievedMessage(Message message) {

        switch (message.command) {
            case AUTH_OK:
                primaryController.setNickName(message.authOkMessage.nickname);
                primaryController.showChatPanel();
                String login = primaryController.getLogin();
                file = new File("history/history_" + login + ".txt");
                getHistory(file);
                break;
            case AUTH_ERROR:
                primaryController.showError(message.authErrorMessage.errorMsg);
                break;
            case PRIVATE_MESSAGE:
                PrivateMessage privateMessage = message.privateMessage;
                String fromPrivate = privateMessage.from;
                String msgPrivate = privateMessage.message;
                String msgToView = String.format("%s (private): %s%n", fromPrivate, msgPrivate);
                chatTextArea.appendText(msgToView);
                System.out.println(primaryController.getLogin() + file);
                addInHistory(msgToView);
                break;
            case PRIVATE_MESSAGE_FROM:
                privateMessage = message.privateMessage;
                String toPrivate = privateMessage.to;
                String msgPrivateFrom = privateMessage.message;
                String msgToViewFrom = String.format("To %s (private): %s%n", toPrivate, msgPrivateFrom);
                chatTextArea.appendText(msgToViewFrom);
                addInHistory(msgToViewFrom);
                break;
            case PUBLIC_MESSAGE:
                PublicMessage publicMessage = message.publicMessage;
                String fromPublic = publicMessage.from;
                String msgPublic = publicMessage.message;
                if (fromPublic != null) {
                    chatTextArea.appendText(String.format("%s: %s%n", fromPublic, msgPublic));
                    addInHistory(String.format("%s: %s%n", fromPublic, msgPublic));
                } else {
                    chatTextArea.appendText(String.format("%s%n", msgPublic));
                    addInHistory(String.format("%s%n", msgPublic));
                }
                break;
            case CLIENT_LIST:
                List<String> onlineUserNicknames = message.clientListMessage.online;
                primaryController.refreshUsersList(onlineUserNicknames);
                break;
            case NICK_CHANGE_OK:
                primaryController.setNickName();
                break;
            case NICK_CHANGE_ERROR:
                primaryController.showError(message.nickChangeErrorMessage.errorMsg);
                break;
            default:
                throw new IllegalArgumentException("Unknown command type: " + message.command);
        }
    }

    private void getHistory(File file) {
        try {
            if (!file.exists())
                file.getParentFile().mkdirs();
                file.createNewFile();
            FileReader fileReader = new FileReader(this.file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            List<String> history = new LinkedList<>();
            while ((line = bufferedReader.readLine()) != null) {
                history.add(line + '\n');
            }
            bufferedReader.close();
            int numberLinesHistory;
            if (history.size() >= NUMBER_LINES_HISTORY) numberLinesHistory = history.size() - 1 - NUMBER_LINES_HISTORY;
                    else numberLinesHistory = 0;
            for (int i = numberLinesHistory; i < history.size();  i++) {
                if (history.size() != 0)
                chatTextArea.appendText(history.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addInHistory(String msgToView) {
        try {
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(msgToView);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        if (needStopServerOnClosed) {
            sendMessage(Message.serverEndMessage());
        }
        network.close();
    }



}
