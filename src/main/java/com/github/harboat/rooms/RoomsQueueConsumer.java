package com.github.harboat.rooms;

import com.github.harboat.clients.exceptions.BadRequest;
import com.github.harboat.clients.exceptions.ResourceNotFound;
import com.github.harboat.clients.notification.EventType;
import com.github.harboat.clients.notification.NotificationRequest;
import com.github.harboat.clients.rooms.*;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@RabbitListener(
        queues = {"${rabbitmq.queues.rooms}"}
)
public class RoomsQueueConsumer {

    private RoomService service;
    private NotificationProducer notificationProducer;

    @RabbitHandler
    public void consume(RoomCreate roomCreate) {
        service.create(roomCreate);
    }

    @RabbitHandler
    public void consume(RoomPlayerJoin roomPlayerJoin) {
        try {
            service.joinPlayer(roomPlayerJoin);
        } catch (BadRequest | ResourceNotFound e) {
            notificationProducer.sendNotification(
                    new NotificationRequest<>(roomPlayerJoin.playerId(), EventType.EXCEPTION, e)
            );
        }
    }

    @RabbitHandler
    public void consume(ChangePlayerReadiness playerReadiness) {
        try {
            service.changeReady(playerReadiness);
        } catch (BadRequest | ResourceNotFound e) {
            notificationProducer.sendNotification(
                    new NotificationRequest<>(playerReadiness.playerId(), EventType.EXCEPTION, e)
            );
        }
    }

    @RabbitHandler
    public void consume(MarkStart markStart) {
        try {
            service.markStart(markStart);
        } catch (BadRequest | ResourceNotFound e) {
            notificationProducer.sendNotification(
                    new NotificationRequest<>(markStart.playerId(), EventType.EXCEPTION, e)
            );
        }
    }

    @RabbitHandler
    public void consume(MarkFleetSet markFleetSet) {
        try {
            service.markFleetSet(markFleetSet);
        } catch (BadRequest | ResourceNotFound e) {
            notificationProducer.sendNotification(
                    new NotificationRequest<>(markFleetSet.playerId(), EventType.EXCEPTION, e)
            );
        }
    }

    @RabbitHandler
    public void consume(UnmarkFleetSet unmarkFleetSet) {
        try {
            service.unmarkFleet(unmarkFleetSet);
        } catch (BadRequest | ResourceNotFound e) {
            notificationProducer.sendNotification(
                    new NotificationRequest<>(unmarkFleetSet.playerId(), EventType.EXCEPTION, e)
            );
        }
    }
}
