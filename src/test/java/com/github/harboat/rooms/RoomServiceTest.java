package com.github.harboat.rooms;

import com.github.harboat.clients.configuration.ConfigurationCreate;
import com.github.harboat.clients.configuration.ConfigurationPlayerJoin;
import com.github.harboat.clients.exceptions.BadRequest;
import com.github.harboat.clients.exceptions.ResourceNotFound;
import com.github.harboat.clients.rooms.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.testng.Assert.*;
import static org.mockito.BDDMockito.*;

@Listeners({MockitoTestNGListener.class})
public class RoomServiceTest {

    @Mock
    private RoomRepository repository;
    @Mock
    private CoreQueueProducer coreQueueProducer;
    @Mock
    private ConfigQueueProducer configQueueProducer;
    @Mock
    private NotificationProducer notificationProducer;
    private RoomService roomService;
    private String roomId;
    private String playerId;

    @BeforeMethod
    public void setUp() {
        roomService = new RoomService(repository, coreQueueProducer, configQueueProducer, notificationProducer);
        roomId = "testRoom";
        playerId = "testPlayer";
    }

    @Test
    public void createShouldSendRoomWithProperRoomCreated() {
        //given
        Room room = Room.builder()
                .id(roomId)
                .build();
        RoomCreate roomCreate = new RoomCreate(playerId);
        given(repository.save(any())).willReturn(room);
        ArgumentCaptor<RoomCreated> captor = ArgumentCaptor.forClass(RoomCreated.class);
        //when
        roomService.create(roomCreate);
        verify(coreQueueProducer).sendRoom(captor.capture());
        var actual = captor.getValue();
        //then
        assertEquals(actual, new RoomCreated(roomId, playerId));
    }

    @Test
    public void createShouldSendCreateWithProperConfigurationCreate() {
        //given
        Room room = Room.builder()
                .id(roomId)
                .build();
        RoomCreate roomCreate = new RoomCreate(playerId);
        given(repository.save(any())).willReturn(room);
        ArgumentCaptor<ConfigurationCreate> captor = ArgumentCaptor.forClass(ConfigurationCreate.class);
        //when
        roomService.create(roomCreate);
        verify(configQueueProducer).sendCreate(captor.capture());
        var actual = captor.getValue();
        //then
        assertEquals(actual, new ConfigurationCreate(roomId, playerId));
    }

    @Test(expectedExceptions = ResourceNotFound.class, expectedExceptionsMessageRegExp = "Couldn't find the room!")
    public void joinPlayerShouldThrowWhenThereIsNoRoomWithThisId() {
        //given
        RoomPlayerJoin roomPlayerJoin = new RoomPlayerJoin(roomId, playerId);
        given(repository.findById(roomId)).willReturn(Optional.empty());
        //when
        roomService.joinPlayer(roomPlayerJoin);
        //then
    }

    @Test(expectedExceptions = BadRequest.class, expectedExceptionsMessageRegExp = "Room is full!")
    public void joinPlayerShouldThrowWhenRoomIsFull() {
        //given
        String enemy = "testEnemy";
        Map<String, Player> players = new HashMap<>() {{
            put(playerId, new Player());
            put(enemy, new Player());
        }};
        Room room = Room.builder()
                .id(roomId)
                .ownerId(playerId)
                .players(players)
                .build();
        RoomPlayerJoin roomPlayerJoin = new RoomPlayerJoin(roomId, playerId);
        given(repository.findById(roomId)).willReturn(Optional.of(room));
        //when
        roomService.joinPlayer(roomPlayerJoin);
        //then
    }

    @Test(expectedExceptions = BadRequest.class, expectedExceptionsMessageRegExp = "You are already in this room!")
    public void joinPlayerShouldThrowWhenPlayerAlreadyInTheRoom() {
        //given
        Map<String, Player> players = new HashMap<>() {{
            put(playerId, new Player());
        }};
        Room room = Room.builder()
                .id(roomId)
                .ownerId(playerId)
                .players(players)
                .build();
        RoomPlayerJoin roomPlayerJoin = new RoomPlayerJoin(roomId, playerId);
        given(repository.findById(roomId)).willReturn(Optional.of(room));
        //when
        roomService.joinPlayer(roomPlayerJoin);
        //then
    }

    @Test
    public void joinPlayerShouldSendWithProperConfigurationPlayerJoin() {
        //given
        Map<String, Player> players = new HashMap<>();
        Room room = Room.builder()
                .id(roomId)
                .ownerId(playerId)
                .players(players)
                .build();
        RoomPlayerJoin roomPlayerJoin = new RoomPlayerJoin(roomId, playerId);
        given(repository.findById(roomId)).willReturn(Optional.of(room));
        ArgumentCaptor<ConfigurationPlayerJoin> captor = ArgumentCaptor.forClass(ConfigurationPlayerJoin.class);
        //when
        roomService.joinPlayer(roomPlayerJoin);
        verify(configQueueProducer).sendPlayerJoin(captor.capture());
        var actual = captor.getValue();
        //then
        assertEquals(actual, new ConfigurationPlayerJoin(roomId, playerId));
    }

    @Test
    public void joinPlayerShouldSendWithProperRoomPlayerJoined() {
        //given
        Map<String, Player> players = new HashMap<>();
        Room room = Room.builder()
                .id(roomId)
                .ownerId(playerId)
                .players(players)
                .build();
        RoomPlayerJoin roomPlayerJoin = new RoomPlayerJoin(roomId, playerId);
        given(repository.findById(roomId)).willReturn(Optional.of(room));
        ArgumentCaptor<RoomPlayerJoined> captor = ArgumentCaptor.forClass(RoomPlayerJoined.class);
        //when
        roomService.joinPlayer(roomPlayerJoin);
        verify(coreQueueProducer).sendPlayerJoin(captor.capture());
        var actual = captor.getValue();
        //then
        assertEquals(actual, new RoomPlayerJoined(roomId, playerId));
    }

    @Test(expectedExceptions = BadRequest.class, expectedExceptionsMessageRegExp = "You are not an owner of this game!")
    public void markStartShouldThrowIfPlayerIsNotTheOwner() {
        //given
        Map<String, Player> players = new HashMap<>() {{
            put(playerId, new Player());
        }};
        Room room = Room.builder()
                .id(roomId)
                .ownerId("")
                .players(players)
                .build();
        given(repository.findById(roomId)).willReturn(Optional.of(room));
        MarkStart markStart = new MarkStart(roomId, playerId);
        //when
        roomService.markStart(markStart);
        //then
    }

    @Test(expectedExceptions = BadRequest.class, expectedExceptionsMessageRegExp = "You can't play solo!")
    public void markStartShouldThrowIfThereIsOnlyOnePlayerInTheRoom() {
        //given
        Map<String, Player> players = new HashMap<>() {{
            put(playerId, new Player());
        }};
        Room room = Room.builder()
                .id(roomId)
                .ownerId(playerId)
                .players(players)
                .build();
        given(repository.findById(roomId)).willReturn(Optional.of(room));
        MarkStart markStart = new MarkStart(roomId, playerId);
        //when
        roomService.markStart(markStart);
        //then
    }

    @Test(expectedExceptions = BadRequest.class, expectedExceptionsMessageRegExp = "Not all players are ready!")
    public void markStartShouldThrowIfNotAllPlayersAreReady() {
        //given
        String enemy = "testEnemy";
        Map<String, Player> players = new HashMap<>() {{
            put(playerId, new Player(true, true));
            put(enemy, new Player(false, false));
        }};
        Room room = Room.builder()
                .id(roomId)
                .ownerId(playerId)
                .players(players)
                .build();
        given(repository.findById(roomId)).willReturn(Optional.of(room));
        MarkStart markStart = new MarkStart(roomId, playerId);
        //when
        roomService.markStart(markStart);
        //then
    }

    @Test(expectedExceptions = BadRequest.class, expectedExceptionsMessageRegExp = "Not all players have fleet set!")
    public void markStartShouldThrowIfNotAllFleetsAreSet() {
        //given
        String enemy = "testEnemy";
        Map<String, Player> players = new HashMap<>() {{
            put(playerId, new Player(true, true));
            put(enemy, new Player(true, false));
        }};
        Room room = Room.builder()
                .id(roomId)
                .ownerId(playerId)
                .players(players)
                .build();
        given(repository.findById(roomId)).willReturn(Optional.of(room));
        MarkStart markStart = new MarkStart(roomId, playerId);
        //when
        roomService.markStart(markStart);
        //then
    }

    @Test
    public void markStartShouldSetRoomStarted() {
        //given
        String enemy = "testEnemy";
        Map<String, Player> players = new HashMap<>() {{
            put(playerId, new Player(true, true));
            put(enemy, new Player(true, true));
        }};
        Room room = Room.builder()
                .id(roomId)
                .ownerId(playerId)
                .players(players)
                .build();
        given(repository.findById(roomId)).willReturn(Optional.of(room));
        MarkStart markStart = new MarkStart(roomId, playerId);
        ArgumentCaptor<Room> captor = ArgumentCaptor.forClass(Room.class);
        //when
        roomService.markStart(markStart);
        verify(repository).save(captor.capture());
        var actual = captor.getValue();
        //then
        assertTrue(actual.getStarted());
    }

    @Test(expectedExceptions = BadRequest.class, expectedExceptionsMessageRegExp = "Player fleet is not set yet, you can't change readiness!")
    public void changeReadyShouldThrowWhenPlayersFleetIsNotSet() {
        //given
        ChangePlayerReadiness playerReadiness = new ChangePlayerReadiness(roomId, playerId);
        Map<String, Player> players = new HashMap<>() {{
            put(playerId, new Player(true, false));
        }};
        Room room = Room.builder()
                .id(roomId)
                .ownerId(playerId)
                .players(players)
                .build();
        given(repository.findById(roomId)).willReturn(Optional.of(room));
        //when
        roomService.changeReady(playerReadiness);
        //then
    }

    @Test
    public void changeReadyShouldChangePlayersReadiness() {
        //given
        ChangePlayerReadiness playerReadiness = new ChangePlayerReadiness(roomId, playerId);
        Map<String, Player> players = new HashMap<>() {{
            put(playerId, new Player(false, true));
        }};
        Room room = Room.builder()
                .id(roomId)
                .ownerId(playerId)
                .players(players)
                .build();
        given(repository.findById(roomId)).willReturn(Optional.of(room));
        ArgumentCaptor<Room> captor = ArgumentCaptor.forClass(Room.class);
        //when
        roomService.changeReady(playerReadiness);
        verify(repository).save(captor.capture());
        var actual = captor.getValue();
        //then
        assertTrue(actual.getPlayers().get(playerId).getReady());
    }

}