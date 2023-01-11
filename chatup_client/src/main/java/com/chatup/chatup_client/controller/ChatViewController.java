package com.chatup.chatup_client.controller;

import com.chatup.chatup_client.component.MessageFactory;
import com.chatup.chatup_client.manager.ChannelManager;
import com.chatup.chatup_client.manager.MessageManager;
import com.chatup.chatup_client.manager.exception.OutOfMessagesException;
import com.chatup.chatup_client.model.Channel;
import com.chatup.chatup_client.model.Message;
import com.chatup.chatup_client.model.UserInfo;
import com.chatup.chatup_client.web.RestClient;
import com.chatup.chatup_client.web.SocketClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javafx.util.Duration;

import javax.annotation.PostConstruct;
import javax.management.RuntimeErrorException;
import java.io.IOException;
import java.io.UncheckedIOException;

import java.util.Collection;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;


@Component
public class ChatViewController extends ViewController {
    final Logger logger = LoggerFactory.getLogger(ChatViewController.class);
    private final MessageManager messageManager;

    private final ChannelManager channelManager;
    
    @FXML
    public ListView<Message> messages;
    final ListChangeListener<Message> listChangeListener = new ListChangeListener<>() {
        @Override
        public void onChanged(Change c) {
            messages.scrollTo(messages.getItems().size() - 1);
        }
    };
    @FXML
    public Button sendButton;
    @FXML
    public TextField message;
    @FXML
    public Rectangle backdrop;
    @FXML
    private HeadbarController headbarController;

    @FXML
    private SidebarController sidebarController;

    @FXML
    private CreateDMDialogController createDMDialogController;

    @FXML
    private CreateChannelDialogController createChannelDialogController;

    private final AtomicBoolean loadingMessagesAfterScroll = new AtomicBoolean(false);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public ChatViewController(MessageManager messageManager, SocketClient socketClient, RestClient restClient, Application application, ChannelManager channelManager) {
        super(socketClient, restClient, application);
        this.messageManager = messageManager;
        this.channelManager = channelManager;
        logger.info("ChatViewController created");
    }

    @PostConstruct
    public void init() {
        channelManager.setChatViewController(this);
    }

    @FXML
    public void onSendMessage(){
        if(currentChannel == null) return;
        logger.info("Text: {}", message.getText());
        if(!message.getText().equals("")){
            socketClient.sendMessage("/app/channel."+currentChannel.getId(), message.getText());
            message.clear();
        }
    }


    public void enableBackdrop(){
        backdrop.setVisible(true);
    }

    public void disableBackdrop(){
        backdrop.setVisible(false);
    }

    @Override
    public void openChannelDialog(){
        enableBackdrop();
        createChannelDialogController.show();
    }

    @Override
    public void closeChannelDialog(){
        disableBackdrop();
        createChannelDialogController.close();
    }

    @Override
    public void openDMDialog(){
        enableBackdrop();
        createDMDialogController.show();
    }

    @Override
    public void closeDMDialog(){
        disableBackdrop();
        createDMDialogController.close();
    }

    @Override
    public void changeChannel(Channel channel){
        if(channel.equals(currentChannel)) {
            sidebarController.channels.refresh();
            sidebarController.direct.refresh();
            return;
        }
        Text placeholder = new Text("This channel is empty. Write your first message!");
        placeholder.setFont(Font.font("Roboto Slab", FontPosture.REGULAR, 20));
        messages.setPlaceholder(placeholder);
        message.setDisable(false);
        logger.info("Changing channel to: " + channel.getName());
        if(currentChannel != null) {
            removeMessagesChangeListener();
        }
        currentChannel = channel;
        sidebarController.channels.refresh();
        sidebarController.direct.refresh();
        messages.setItems(messageManager.getMessageBuffer(currentChannel.getId()).getMessages());
        addMessagesChangeListener();
        try {
            messageManager.getMessageBuffer(currentChannel.getId()).loadNextMessages();
        } catch (OutOfMessagesException ignored) {} // TODO: Handle this exception

        int messagesSize = messageManager.getMessageBuffer(channel.getId()).getMessages().size();
        messages.scrollTo(messagesSize - 1);
    }

    @Override
    public void createDM(Long userId){
        UserInfo currentUser = restClient.getCurrentUser();
        if(userId == currentUser.getId()) return;
        HashSet<Long> userIds = new HashSet<>();
        userIds.add(userId);
        userIds.add(currentUser.getId());
        Channel newChannel = restClient.createChannel("", true,  true, userIds);
        this.changeChannel(newChannel);

    }

    @Override
    public void scrollToMessage(Message message) {
        Channel channel = channelManager.getChannelForMessage(message);
        if(channel == null) {
            throw new RuntimeException();
        }
        changeChannel(channel);
        removeMessagesChangeListener();
        try {
            while (!messageManager.getMessageBuffer(channel.getId()).getMessages().contains(message)) {
                messageManager.getMessageBuffer(channel.getId()).loadNextMessages();
            }
        }
        catch (OutOfMessagesException e) {
            throw new RuntimeException();
        }

        Platform.runLater(() -> {
            messages.scrollTo(message);
            addMessagesChangeListener();
        });
    }

    @FXML
    public void setOnKeyPressed(KeyEvent e){
        if(e.getCode() == KeyCode.ENTER){
            onSendMessage();
        }
    }

    private void setCellFactories() {
        messages.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (item != null) {
                    setMaxWidth(param.getWidth());
                    setMinWidth(param.getWidth() - 100);
                    setPrefWidth(param.getWidth() - 100);

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    GridPane message = MessageFactory.createMessage(item.getContent(),
                            item.getAuthorFirstName() + " " + item.getAuthorLastName(), item.getAuthorUsername(), param.getWidth());

                    setGraphic(message);

                }

                setOnMouseClicked(e->createDM(item.getAuthorID()));
            }
        });
    }

    @Override
    public void initialize(java.net.URL location, ResourceBundle resources) {
        headbarController.setHeadController(this);
        sidebarController.setHeadController(this);
        createDMDialogController.setHeadController(this);
        createChannelDialogController.setHeadController(this);
        setCellFactories();

        backdrop.setVisible(false);
        sidebarController.addDM.setVisible(true);
        sidebarController.addChannel.setVisible(true);
        closeDMDialog();
        closeChannelDialog();

        setMessagesScrollHandler();

        Text placeholder = new Text("Hello! Choose your channel");
        placeholder.setFont(Font.font("Roboto Slab", FontPosture.REGULAR, 20));
        messages.setPlaceholder(placeholder);
        message.setDisable(true);

        try{
            socketClient.connect();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void setMessagesScrollHandler() {
        messages.setOnScroll(e -> {
            if(currentChannel != null &&
                    e.getDeltaY() > 0 &&
                    loadingMessagesAfterScroll.compareAndSet(false, true)
            ) {
                removeMessagesChangeListener();
                int prevSize = messages.getItems().size();
                try {
                    messageManager.getMessageBuffer(currentChannel.getId()).loadNextMessages();
                } catch (OutOfMessagesException ex) {}
                int currentSize = messages.getItems().size();
                if(currentSize > 0)
                    messages.scrollTo(Math.max(currentSize - prevSize - 1, 0));
                addMessagesChangeListener();
                loadingMessagesAfterScroll.set(false);
            }
        });
    }

    private void addMessagesChangeListener() {
        if(currentChannel != null)
            messageManager.getMessageBuffer(currentChannel.getId()).getMessages().addListener(listChangeListener);
    }

    private void removeMessagesChangeListener() {
        if(currentChannel != null)
            messageManager.getMessageBuffer(currentChannel.getId()).getMessages().removeListener(listChangeListener);
    }

}
