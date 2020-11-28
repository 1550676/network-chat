package ru.zakharova.elena.network.chat.client;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import ru.zakharova.elena.network.chat.client.service.IMessageService;
import ru.zakharova.elena.network.chat.client.service.ServerMessageService;
import ru.zakharova.elena.network.chat.common.Message;

import static ru.zakharova.elena.network.chat.common.Message.createAuth;
import static ru.zakharova.elena.network.chat.common.Message.createNickChange;


public class PrimaryController implements Initializable {

    private static final String ALL_ITEM = "ALL";
    public @FXML
    TextArea chatTextArea;
    public @FXML TextField messageText;
    public @FXML
    Button sendMessageButton;

    public @FXML TextField loginField;
    public @FXML
    PasswordField passField;

    public @FXML HBox authPanel;
    public @FXML VBox chatPanel;

    public @FXML
    TextField newNickField;
    public @FXML HBox nickChangePanel;


    public @FXML
    ListView<String> clientList;


    private IMessageService messageService;
    private String nickName;
    private String login;

    public String getLogin() {
        return login;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            this.messageService = new ServerMessageService(this, true);
        } catch (Exception e) {
            showError(e);
        }
    }

    private void showError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("oops! Something went wrong!");
        alert.setHeaderText(e.getMessage());

        VBox dialogPaneContent = new VBox();
        Label label = new Label("Stack Trace:");

        String stackTrace = ExceptionUtils.getStackTrace(e);
        TextArea textArea = new TextArea();
        textArea.setText(stackTrace);

        dialogPaneContent.getChildren().addAll(label, textArea);

        // Set content for Dialog Pane
        alert.getDialogPane().setContent(dialogPaneContent);
        alert.setResizable(true);
        alert.showAndWait();

        e.printStackTrace();
    }

    @FXML
    public void sendText(ActionEvent actionEvent) {
        sendMessage();
    }

    @FXML
    public void sendMessage(ActionEvent actionEvent) {
        sendMessage();
    }

    private void sendMessage() {
        String message = messageText.getText();
        if (StringUtils.isNotBlank(message)) {
            Message msg = buildMessage(message);
            messageService.sendMessage(msg);
            messageText.clear();
        }
    }

    private Message buildMessage(String message) {
        String selectedNickname = clientList.getSelectionModel().getSelectedItem();
        if (selectedNickname != null && !selectedNickname.equals(ALL_ITEM)) {
            return Message.createPrivate(nickName, selectedNickname, message);
        }

        return Message.createPublic(nickName, message);
    }

    public void shutdown() {
        try {
            messageService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void sendAuth(ActionEvent actionEvent) {
        login = loginField.getText();
        String password = passField.getText();
        messageService.sendMessage(createAuth(login, password));
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
        Stage stage = (Stage) chatPanel.getScene().getWindow();
        stage.setTitle(nickName);
    }

    public void setNickName() {
        this.nickName = newNickField.getText();
        Stage stage = (Stage) chatPanel.getScene().getWindow();
        stage.setTitle(newNickField.getText());
    }

    public void showChatPanel() {
        authPanel.setVisible(false);
        chatPanel.setVisible(true);
    }



    public void showError(String errorMsg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ошибка");
        alert.setContentText(errorMsg);
        alert.showAndWait();
    }

    public void refreshUsersList(List<String> onlineUserNicknames) {
        onlineUserNicknames.add(ALL_ITEM);
        clientList.setItems(FXCollections.observableArrayList(onlineUserNicknames));
    }

    public void sendNickChange(ActionEvent actionEvent) {
        String newNick = newNickField.getText();
        messageService.sendMessage(createNickChange(newNick));
    }

    public String getNickName() {
        return nickName;
    }
}
