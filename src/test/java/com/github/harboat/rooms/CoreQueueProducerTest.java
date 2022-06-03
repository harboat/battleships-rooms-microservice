package com.github.harboat.rooms;

import com.github.harboat.clients.rooms.RoomCreated;
import com.github.harboat.clients.rooms.RoomGameStart;
import com.github.harboat.clients.rooms.RoomPlayerJoined;
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
public class CoreQueueProducerTest {

    @Mock
    private RabbitMQMessageProducer producer;
    private CoreQueueProducer coreQueueProducer;

    @BeforeMethod
    public void setUp() {
        coreQueueProducer = new CoreQueueProducer(producer);
    }

    @Test
    public void sendRoomShouldPublishWithProperRoomCreated () {
        //given
        RoomCreated roomCreated = new RoomCreated("testRoom", "testOwner");
        ArgumentCaptor<RoomCreated> roomCaptor = ArgumentCaptor.forClass(RoomCreated.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        //when
        coreQueueProducer.sendRoom(roomCreated);
        verify(producer).publish(roomCaptor.capture(), captor.capture(), captor.capture());
        var actual = roomCaptor.getValue();
        //then
        assertEquals(actual, roomCreated);
    }

    @Test
    public void sendStartShouldPublishWithProperRoomGameStart () {
        //given
        RoomGameStart roomGameStart = new RoomGameStart("testRoom");
        ArgumentCaptor<RoomGameStart> roomCaptor = ArgumentCaptor.forClass(RoomGameStart.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        //when
        coreQueueProducer.sendStart(roomGameStart);
        verify(producer).publish(roomCaptor.capture(), captor.capture(), captor.capture());
        var actual = roomCaptor.getValue();
        //then
        assertEquals(actual, roomGameStart);
    }

    @Test
    public void sendPlayerJoinShouldPublishWithProperRoomPlayerJoined () {
        //given
        RoomPlayerJoined roomPlayerJoined = new RoomPlayerJoined("testRoom", "testPlayer");
        ArgumentCaptor<RoomPlayerJoined> roomCaptor = ArgumentCaptor.forClass(RoomPlayerJoined.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        //when
        coreQueueProducer.sendPlayerJoin(roomPlayerJoined);
        verify(producer).publish(roomCaptor.capture(), captor.capture(), captor.capture());
        var actual = roomCaptor.getValue();
        //then
        assertEquals(actual, roomPlayerJoined);
    }
}