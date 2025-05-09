package com.ucentral.rabbitmq_app.services;

import com.ucentral.rabbitmq_app.RabbitMQConfig;
import com.ucentral.rabbitmq_app.dto.CleaningTaskUIDTO;
import com.ucentral.rabbitmq_app.events.CleaningTaskUpdatedEvent;
import com.ucentral.rabbitmq_app.events.NewCleaningTaskEvent;
import com.ucentral.rabbitmq_app.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class CleaningService {

   private static final Logger log = LoggerFactory.getLogger(CleaningService.class);
   private final Map<Long, CleaningTaskUIDTO> pendingCleaningTasks = new ConcurrentHashMap<>();
   private final Map<Long, CleaningTaskUIDTO> cleanedTasks = new ConcurrentHashMap<>();

   private final ApplicationEventPublisher eventPublisher;

   @Autowired
   public CleaningService(ApplicationEventPublisher eventPublisher) {
      this.eventPublisher = eventPublisher;
   }

   @RabbitListener(queues = RabbitMQConfig.QUEUE_LIMPIEZA)
   public void handleConfirmedReservationForCleaning(@Payload Reservation reservation) {
      log.info("CleaningService: Reserva recibida para limpieza (Cola: {}): ResID {}, Habitación {}",
            RabbitMQConfig.QUEUE_LIMPIEZA, reservation.getId(), reservation.getRoom().getRoomNumber());

      if (reservation.getRoom() == null) {
         log.warn(
               "CleaningService: Reserva ID {} no tiene detalles de habitación. No se puede crear tarea de limpieza.",
               reservation.getId());
         return;
      }

      if (pendingCleaningTasks.containsKey(reservation.getId()) || cleanedTasks.containsKey(reservation.getId())) {
         log.info("CleaningService: Tarea de limpieza para ResID {} ya existe o está completada. Ignorando.",
               reservation.getId());
         return;
      }

      CleaningTaskUIDTO newTask = new CleaningTaskUIDTO(
            reservation.getId(),
            reservation.getRoom().getId(),
            reservation.getRoom().getRoomNumber(),
            reservation.getCheckOutDate() // Cleaning usually based on check-out
      );

      pendingCleaningTasks.put(newTask.getReservationId(), newTask);
      log.info("CleaningService: Nueva tarea de limpieza añadida: {}", newTask);
      eventPublisher.publishEvent(new NewCleaningTaskEvent(this, newTask));
   }

   public void markTaskAsCleaned(Long reservationId) {
      CleaningTaskUIDTO taskToMove = pendingCleaningTasks.remove(reservationId);
      if (taskToMove != null) {
         taskToMove.setStatus("CLEANED");
         cleanedTasks.put(taskToMove.getReservationId(), taskToMove);
         log.info("CleaningService: Tarea marcada como limpiada: {}", taskToMove);
         eventPublisher.publishEvent(new CleaningTaskUpdatedEvent(this, taskToMove));
      } else {
         log.warn(
               "CleaningService: No se pudo marcar la tarea como limpiada. No se encontró tarea pendiente con ResID: {}",
               reservationId);
      }
   }

   public List<CleaningTaskUIDTO> getPendingCleaningTasks() {
      return new ArrayList<>(pendingCleaningTasks.values());
   }

   public List<CleaningTaskUIDTO> getCleanedTasks() {
      return new ArrayList<>(cleanedTasks.values());
   }
}