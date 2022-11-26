package com.chatup.chatup_client.model;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.LinkedList;
import java.util.List;

public class MessageBuffer {

    public ObservableList<Message> getMessages() {
        return messages;
    }

    private final ObservableList<Message> messages;

    public MessageBuffer() {
        this.messages = FXCollections.observableArrayList();
        messages.add(new Message("Dupa"));
//        messages.addListener(listener);
    }

    public void addMessage(Message msg){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                messages.add(msg);
            }
        });
    }

}