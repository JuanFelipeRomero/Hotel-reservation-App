package com.ucentral.rabbitmq_app.services;

import com.ucentral.rabbitmq_app.RabbitMQConfig;
import com.ucentral.rabbitmq_app.dto.CleaningTaskUIDTO;
import com.ucentral.rabbitmq_app.dto.FinalBookingDetailsDTO;
import com.ucentral.rabbitmq_app.events.CleaningTaskUpdatedEvent;
import com.ucentral.rabbitmq_app.events.NewCleaningTaskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
   public void handleConfirmedReservationForCleaning(@Payload FinalBookingDetailsDTO bookingDetails) {
      log.info("CleaningService: Detalles de reserva recibidos para limpieza (Cola: {}): RoomID {}, Room# {}",
            RabbitMQConfig.QUEUE_LIMPIEZA, bookingDetails.getRoomId(), bookingDetails.getRoomNumber());

      if (bookingDetails.getRoomId() == null || bookingDetails.getRoomNumber() == null) {
         log.warn(
               "CleaningService: Detalles de reserva incompletos (RoomID o RoomNumber es null). No se puede crear tarea de limpieza. DTO: {}",
               bookingDetails);
         return;
      }

      Long taskKey = bookingDetails.getRoomId();

      if (pendingCleaningTasks.containsKey(taskKey) || cleanedTasks.containsKey(taskKey)) {
         log.info("CleaningService: Tarea de limpieza para RoomID {} ya existe o está completada. Ignorando.",
               taskKey);
         return;
      }

      LocalDate checkInDate;
      try {
         checkInDate = LocalDate.parse(bookingDetails.getCheckInDate());
      } catch (DateTimeParseException e) {
         log.error(
               "CleaningService: Error parsing check-out date '{}' from booking details. Cannot create cleaning task.",
               bookingDetails.getCheckInDate(), e);
         return;
      }

      CleaningTaskUIDTO newTask = new CleaningTaskUIDTO(
            taskKey,
            bookingDetails.getRoomId(),
            bookingDetails.getRoomNumber(),
            checkInDate);

      pendingCleaningTasks.put(newTask.getReservationId(), newTask);
      log.info("CleaningService: Nueva tarea de limpieza añadida: {}", newTask);
      eventPublisher.publishEvent(new NewCleaningTaskEvent(this, newTask));
   }

   public void markTaskAsCleaned(Long taskKey) {
      CleaningTaskUIDTO taskToMove = pendingCleaningTasks.remove(taskKey);
      if (taskToMove != null) {
         taskToMove.setStatus("CLEANED");
         cleanedTasks.put(taskToMove.getReservationId(), taskToMove);
         log.info("CleaningService: Tarea marcada como limpiada: {}", taskToMove);
         eventPublisher.publishEvent(new CleaningTaskUpdatedEvent(this, taskToMove));
      } else {
         log.warn(
               "CleaningService: No se pudo marcar la tarea como limpiada. No se encontró tarea pendiente con ID: {}",
               taskKey);
      }
   }

   public List<CleaningTaskUIDTO> getPendingCleaningTasks() {
      return new ArrayList<>(pendingCleaningTasks.values());
   }

   public List<CleaningTaskUIDTO> getCleanedTasks() {
      return new ArrayList<>(cleanedTasks.values());
   }
}