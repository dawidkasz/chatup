package com.chatup.chatup_server.client;

import com.chatup.chatup_server.service.messaging.IncomingMessage;
import com.chatup.chatup_server.service.messaging.OutgoingMessage;

import java.util.List;

public interface SocketClient {
    void sendMessage(String topic, String msg);
    List<OutgoingMessage> getMessages();
    void subscribe(String topic);
    void close();
}
