package com.github.harboat.rooms;

import com.github.harboat.clients.configuration.ConfigurationCreate;
import com.github.harboat.clients.configuration.ConfigurationPlayerJoin;
import com.github.harboat.clients.configuration.CreateGame;
import com.github.harboat.rabbitmq.RabbitMQMessageProducer;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.mockito.BDDMockito.*;

@Listeners({MockitoTestNGListener.class})
public class ConfigQueueProducerTest {

    @Mock
    private RabbitMQMessageProducer rabbitMQMessageProducer;
    private ConfigQueueProducer producer;

    @BeforeMethod
    public void setUp() {
        producer = new ConfigQueueProducer(rabbitMQMessageProducer);
    }

    @Test
    public void sendCreateShouldPublishWithProperConfigurationCreate() {
        //given
        ConfigurationCreate config = new ConfigurationCreate("testRoom", "testPlayer");
        ArgumentCaptor<ConfigurationCreate> configurationCaptor = ArgumentCaptor.forClass(ConfigurationCreate.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        //when
        producer.sendCreate(config);
        verify(rabbitMQMessageProducer).publish(configurationCaptor.capture(),captor.capture(),captor.capture());
        var actual = configurationCaptor.getValue();
        //then
        assertEquals(actual, config);
    }

    @Test
    public void sendCreateGameShouldPublishWithProperCreateGame() {
        //given
        CreateGame createGame = new CreateGame("testRoom", "testPlayer");
        ArgumentCaptor<CreateGame> configurationCaptor = ArgumentCaptor.forClass(CreateGame.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        //when
        producer.sendCreateGame(createGame);
        verify(rabbitMQMessageProducer).publish(configurationCaptor.capture(),captor.capture(),captor.capture());
        var actual = configurationCaptor.getValue();
        //then
        assertEquals(actual, createGame);
    }

    @Test
    public void sendPlayerJoinShouldPublishWithProperConfiguration() {
        //given
        ConfigurationPlayerJoin config = new ConfigurationPlayerJoin("testRoom", "testPlayer");
        ArgumentCaptor<ConfigurationPlayerJoin> configurationCaptor = ArgumentCaptor.forClass(ConfigurationPlayerJoin.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        //when
        producer.sendPlayerJoin(config);
        verify(rabbitMQMessageProducer).publish(configurationCaptor.capture(),captor.capture(), captor.capture());
        var actual = configurationCaptor.getValue();
        //then
        assertEquals(actual, config);
    }
}