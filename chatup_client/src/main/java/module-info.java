module com.chatup.chatup_client {
    requires javafx.controls;
    requires javafx.fxml;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires org.apache.tomcat.embed.websocket;
    requires spring.websocket;
    requires spring.messaging;
    requires org.slf4j;


    opens com.chatup.chatup_client to javafx.fxml;
    exports com.chatup.chatup_client;
    exports com.chatup.chatup_client.controller;
    exports com.chatup.chatup_client.model;
    opens com.chatup.chatup_client.controller to javafx.fxml;
}