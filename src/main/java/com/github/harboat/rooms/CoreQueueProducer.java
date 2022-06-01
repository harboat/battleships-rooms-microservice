package com.github.harboat.rooms;

import com.github.harboat.clients.rooms.RoomCreated;
import com.github.harboat.clients.rooms.RoomGameStart;
import com.github.harboat.clients.rooms.RoomPlayerJoined;
import com.github.harboat.rabbitmq.RabbitMQMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoreQueueProducer {

    private final RabbitMQMessageProducer producer;

    @Value("${rabbitmq.exchanges.core}")
    private String internalExchange;

    @Value("${rabbitmq.routing-keys.core}")
    private String coreRoutingKey;

    void sendRoom(RoomCreated roomCreated) {
        producer.publish(roomCreated, internalExchange, coreRoutingKey);
    }

    public void sendStart(RoomGameStart roomGameStart) {
        producer.publish(roomGameStart, internalExchange, coreRoutingKey);
    }

    public void sendPlayerJoin(RoomPlayerJoined roomPlayerJoined) {
        producer.publish(roomPlayerJoined, internalExchange, coreRoutingKey);
    }
}
