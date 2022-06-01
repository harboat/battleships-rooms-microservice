package com.github.harboat.rooms;

import com.github.harboat.clients.configuration.ConfigurationCreate;
import com.github.harboat.clients.configuration.ConfigurationPlayerJoin;
import com.github.harboat.clients.configuration.CreateGame;
import com.github.harboat.clients.exceptions.BadRequest;
import com.github.harboat.clients.exceptions.ResourceNotFound;
import com.github.harboat.clients.notification.EventType;
import com.github.harboat.clients.notification.NotificationRequest;
import com.github.harboat.clients.rooms.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class RoomService {

    private RoomRepository repository;
    private CoreQueueProducer coreQueueProducer;
    private ConfigQueueProducer configQueueProducer;
    private NotificationProducer notificationProducer;

    public void create(RoomCreate roomCreate) {
        Player player = Player.builder()
                .ready(false)
                .fleetSet(false)
                .build();
        Room room = repository.save(
                Room.builder()
                        .players(Map.of(
                                roomCreate.playerId(), player
                        ))
                        .ownerId(roomCreate.playerId())
                        .started(false)
                        .build()
        );
        coreQueueProducer.sendRoom(new RoomCreated(room.getId(), roomCreate.playerId()));
        configQueueProducer.sendCreate(
                new ConfigurationCreate(room.getId(), roomCreate.playerId())
        );
    }

    public void markFleetSet(MarkFleetSet markFleetSet) {
        Room room = getRoomFromRequest(markFleetSet.roomId(), markFleetSet.playerId());
        room.markPlayerFleetSet(markFleetSet.playerId());
        repository.save(room);
    }

    public void unmarkFleet(UnmarkFleetSet unmarkFleetSet) {
        Room room = getRoomFromRequest(unmarkFleetSet.roomId(), unmarkFleetSet.playerId());
        room.unmarkFleetSets();
        repository.save(room);
    }

    public void changeReady(ChangePlayerReadiness playerReadiness) {
        Room room = getRoomFromRequest(playerReadiness.roomId(), playerReadiness.playerId());
        if (!room.isPlayerFleetSet(playerReadiness.playerId()))
            throw new BadRequest("Player fleet is not set yet, you can't change readiness!");
        boolean oldValue = room.changePlayerReadiness(playerReadiness.playerId());
        repository.save(room);
        room.getPlayers().keySet().forEach(p -> {
            notificationProducer.sendNotification(
                    new NotificationRequest<>(p, oldValue ? EventType.PLAYER_UNREADY : EventType.PLAYER_READY, playerReadiness)
            );
        });
    }

    public void markStart(MarkStart markStart) {
        Room room = getRoomFromRequest(markStart.roomId(), markStart.playerId());
        if (!room.isPlayerAnOwner(markStart.playerId())) throw new BadRequest("You are not an owner of this game!");
        if (room.getPlayers().size() != 2) throw new BadRequest("You can't play solo!");
        if (!room.areAllPlayersReady()) throw new BadRequest("Not all players are ready!");
        if (!room.areAllFleetsSet()) throw new BadRequest("Not all players have fleet set!");
        room.setStarted(true);
        repository.save(room);
        coreQueueProducer.sendStart(new RoomGameStart(markStart.roomId()));
        configQueueProducer.sendCreateGame(
                new CreateGame(room.getId(), markStart.playerId())
        );
    }

    private Room getRoomFromRequest(String roomId, String playerId) {
        Room room = repository.findById(roomId).orElseThrow(() -> new ResourceNotFound("Couldn't find the game!"));
        if (!room.isPlayerInTheRoom(playerId)) throw new BadRequest("Player is not in the game!");
        return room;
    }

    public void joinPlayer(RoomPlayerJoin roomPlayerJoin) {
        Room room = repository.findById(roomPlayerJoin.roomId()).orElseThrow(() -> new ResourceNotFound("Couldn't find the room!"));
        if (room.getPlayers().size() == 2) throw new BadRequest("Room is full!");
        if (room.getPlayers().containsKey(roomPlayerJoin.playerId())) throw new BadRequest("You are already in this room!");
        room.addPlayer(roomPlayerJoin.playerId());
        repository.save(room);
        configQueueProducer.sendPlayerJoin(
                new ConfigurationPlayerJoin(roomPlayerJoin.roomId(), roomPlayerJoin.playerId())
        );
        coreQueueProducer.sendPlayerJoin(
                new RoomPlayerJoined(roomPlayerJoin.roomId(), roomPlayerJoin.playerId())
        );
    }
}
