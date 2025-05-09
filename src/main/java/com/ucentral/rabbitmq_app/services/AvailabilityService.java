package com.ucentral.rabbitmq_app.services;

import com.ucentral.rabbitmq_app.RabbitMQConfig;
import com.ucentral.rabbitmq_app.dto.AvailabilityRequestDTO;
import com.ucentral.rabbitmq_app.dto.RoomAvailableEventData;
import com.ucentral.rabbitmq_app.dto.RoomNotAvailableEventData;
import com.ucentral.rabbitmq_app.model.Reservation;
import com.ucentral.rabbitmq_app.model.Room;
import com.ucentral.rabbitmq_app.model.RoomType;
import com.ucentral.rabbitmq_app.repository.ReservationRepository;
import com.ucentral.rabbitmq_app.repository.RoomRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

   private final RoomRepository roomRepository;
   private final ReservationRepository reservationRepository;
   private final ApplicationEventPublisher eventPublisher;
   private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

   @Autowired
   public AvailabilityService(RoomRepository roomRepository, ReservationRepository reservationRepository,
         ApplicationEventPublisher eventPublisher) {
      this.roomRepository = roomRepository;
      this.reservationRepository = reservationRepository;
      this.eventPublisher = eventPublisher;
   }

   @RabbitListener(queues = RabbitMQConfig.QUEUE_RESERVACIONES_PARA_DISPONIBILIDAD)
   public void handleAvailabilityRequest(@Payload AvailabilityRequestDTO requestDTO) {
      System.out.println("Datos recibidos de RabbitMQ (Cola: "
            + RabbitMQConfig.QUEUE_RESERVACIONES_PARA_DISPONIBILIDAD + ") -> " + requestDTO);

      LocalDate checkInDate;
      LocalDate checkOutDate;
      RoomType roomType;
      Room availableRoom = null; // To store the found room

      try {
         checkInDate = LocalDate.parse(requestDTO.getCheckInDate(), DATE_FORMATTER);
         checkOutDate = LocalDate.parse(requestDTO.getCheckOutDate(), DATE_FORMATTER);
         roomType = RoomType.valueOf(requestDTO.getRoomType().toUpperCase());
      } catch (DateTimeParseException | IllegalArgumentException e) {
         System.err.println("Servicio Listener: Error al parsear DTO: " + e.getMessage());
         return;
      }

      String availabilityMessage = "No hay habitaciones de tipo " + roomType
            + " disponibles para las fechas seleccionadas.";
      boolean wasRoomAvailable = false;

      List<Room> roomsOfType = roomRepository.findAll().stream()
            .filter(room -> room.getRoomType() == roomType)
            .collect(Collectors.toList());

      if (roomsOfType.isEmpty()) {
         availabilityMessage = "No existen habitaciones de tipo " + roomType + " en el hotel.";
      } else {
         for (Room room : roomsOfType) {
            List<Reservation> conflictingReservations = reservationRepository.findAll().stream()
                  .filter(reservation -> reservation.getRoom().getId().equals(room.getId()))
                  .filter(reservation -> !checkOutDate.isBefore(reservation.getCheckInDate()) &&
                        !checkInDate.isAfter(reservation.getCheckOutDate()))
                  .collect(Collectors.toList());

            if (conflictingReservations.isEmpty()) {
               availableRoom = room;
               wasRoomAvailable = true;
               availabilityMessage = String.format(
                     "¡Habitación Disponible! Número: %s, Tipo: %s, Precio: $%.2f Para fechas: %s a %s",
                     room.getRoomNumber(), room.getRoomType(), room.getPricePerNight(),
                     checkInDate.format(DATE_FORMATTER), checkOutDate.format(DATE_FORMATTER));
               break;
            }
         }
      }
      System.out.println("Resultado de Validación -> " + availabilityMessage);

      if (wasRoomAvailable && availableRoom != null) {
         RoomAvailableEventData eventData = new RoomAvailableEventData(
               availableRoom.getId(),
               availableRoom.getRoomNumber(),
               availableRoom.getRoomType(),
               availableRoom.getPricePerNight(),
               checkInDate.format(DATE_FORMATTER),
               checkOutDate.format(DATE_FORMATTER));
         eventPublisher.publishEvent(eventData);
         System.out.println("evento de habitacion disponible publicado con datos: " + eventData);
      } else {
         RoomNotAvailableEventData notAvailableEventData = new RoomNotAvailableEventData(
               checkInDate.format(DATE_FORMATTER),
               checkOutDate.format(DATE_FORMATTER),
               roomType);
         eventPublisher.publishEvent(notAvailableEventData);
         System.out
               .println("No se encontró habitación disponible, RoomNotAvailableEvent publicado.");
      }

      StringBuilder resultBuilder = new StringBuilder(availabilityMessage);
      List<Reservation> reservationsForType = reservationRepository.findByRoom_RoomType(roomType);
      resultBuilder.append("\n\n--- Reservas Existentes para Tipo de Habitación: ").append(roomType).append(" ---");
      if (reservationsForType.isEmpty()) {
         resultBuilder.append("\nNo se encontraron reservas para este tipo de habitación.");
      } else {
         for (Reservation res : reservationsForType) {
            resultBuilder.append(String.format("\nHuésped: %s, Habitación: %s, Fechas: %s a %s",
                  res.getGuestName(), res.getRoom().getRoomNumber(),
                  res.getCheckInDate().format(DATE_FORMATTER), res.getCheckOutDate().format(DATE_FORMATTER)));
         }
      }
      System.out.println(
            "Registro de Información Detallada ->\n" + resultBuilder.toString());
   }
}