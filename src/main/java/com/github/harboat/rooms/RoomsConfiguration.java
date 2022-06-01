package com.github.harboat.rooms;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoomsConfiguration {

    @Value("${rabbitmq.exchanges.rooms}")
    private String internalGameExchange;

    @Value("${rabbitmq.queues.rooms}")
    private String roomsQueue;

    @Value("${rabbitmq.routing-keys.rooms}")
    private String internalRoomsRoutingKey;

    @Bean
    public TopicExchange internalTopicExchange() {
        return new TopicExchange(internalGameExchange);
    }

    @Bean
    public Queue roomsQueue() {
        return new Queue(roomsQueue);
    }

    @Bean
    public Binding internalToPlacementBinding() {
        return BindingBuilder
                .bind(roomsQueue())
                .to(internalTopicExchange())
                .with(internalRoomsRoutingKey);
    }

}
