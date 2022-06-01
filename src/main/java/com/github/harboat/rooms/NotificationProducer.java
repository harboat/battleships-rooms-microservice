package com.github.harboat.rooms;

import com.github.harboat.clients.notification.NotificationRequest;
import com.github.harboat.rabbitmq.RabbitMQMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitMQMessageProducer producer;

    @Value("${rabbitmq.exchanges.notification}")
    private String internalExchange;

    @Value("${rabbitmq.routing-keys.notification}")
    private String notificationRoutingKey;

    public void sendNotification(NotificationRequest<?> notification) {
        producer.publish(notification, internalExchange, notificationRoutingKey);
    }

}

