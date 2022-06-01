package com.github.harboat.rooms;

import com.github.harboat.clients.configuration.ConfigurationCreate;
import com.github.harboat.clients.configuration.ConfigurationPlayerJoin;
import com.github.harboat.clients.configuration.CreateGame;
import com.github.harboat.rabbitmq.RabbitMQMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConfigQueueProducer {

    private final RabbitMQMessageProducer producer;

    @Value("${rabbitmq.exchanges.config}")
    private String internalExchange;

    @Value("${rabbitmq.routing-keys.config}")
    private String configRoutingKey;

    public void sendCreate(ConfigurationCreate configurationCreate) {
        producer.publish(configurationCreate, internalExchange, configRoutingKey);
    }

    public void sendCreateGame(CreateGame createGame) {
        producer.publish(createGame, internalExchange, configRoutingKey);
    }

    public void sendPlayerJoin(ConfigurationPlayerJoin configurationPlayerJoin) {
        producer.publish(configurationPlayerJoin, internalExchange, configRoutingKey);
    }
}
