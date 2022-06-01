package com.github.harboat.rooms;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @ToString
@Builder
public class Room {
    @Id
    private String id;
    private Map<String, Player> players;
    private String ownerId;
    private Boolean started;

    void addPlayer(String player) {
        players.put(
                player,
                Player.builder()
                        .ready(false)
                        .fleetSet(false)
                        .build()
        );
    }

    boolean isPlayerInTheRoom(String playerId) {
        return players.containsKey(playerId);
    }

    boolean isPlayerAnOwner(String playerId) {
        return ownerId.equals(playerId);
    }

    boolean areAllFleetsSet() {
        return players.values().stream()
                .allMatch(Player::getFleetSet);
    }

    boolean isPlayerFleetSet(String playerId) {
        return players.get(playerId)
                .getFleetSet();
    }

    void markPlayerFleetSet(String playerId) {
        players.get(playerId).setFleetSet(true);
    }

    void unmarkFleetSets() {
        players.values().forEach(p -> {
            p.setFleetSet(false);
            p.setReady(false);
        });
    }

    boolean areAllPlayersReady() {
        return players.values().stream()
                .allMatch(Player::getReady);
    }

    boolean changePlayerReadiness(String playerId) {
        Player player = players.get(playerId);
        Boolean ready = player.getReady();
        player.setReady(!ready);
        return ready;
    }

}
