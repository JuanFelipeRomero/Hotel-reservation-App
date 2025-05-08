package com.ucentral.rabbitmq_app.services;

import com.ucentral.rabbitmq_app.RabbitMQConfig; // For queue name constant
import com.ucentral.rabbitmq_app.dto.AvailabilityRequestDTO;
import com.ucentral.rabbitmq_app.model.Reservation;
import com.ucentral.rabbitmq_app.model.Room;
import com.ucentral.rabbitmq_app.model.RoomType;
import com.ucentral.rabbitmq_app.repository.ReservationRepository;
import com.ucentral.rabbitmq_app.repository.RoomRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
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
   private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

   @Autowired
   public AvailabilityService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
      this.roomRepository = roomRepository;
      this.reservationRepository = reservationRepository;
   }

   @RabbitListener(queues = RabbitMQConfig.QUEUE_RESERVACIONES_PARA_DISPONIBILIDAD)
   public void handleAvailabilityRequest(@Payload AvailabilityRequestDTO requestDTO) {
      System.out.println("Service Listener: Data received from RabbitMQ (Queue: "
            + RabbitMQConfig.QUEUE_RESERVACIONES_PARA_DISPONIBILIDAD + ") -> " + requestDTO);

      LocalDate checkInDate;
      LocalDate checkOutDate;
      RoomType roomType;

      try {
         checkInDate = LocalDate.parse(requestDTO.getCheckInDate(), DATE_FORMATTER);
         checkOutDate = LocalDate.parse(requestDTO.getCheckOutDate(), DATE_FORMATTER);
         roomType = RoomType.valueOf(requestDTO.getRoomType().toUpperCase()); // Assuming DTO sends enum name as string
      } catch (DateTimeParseException e) {
         System.err.println("AvailabilityService (Listener): Error parsing dates from DTO: " + e.getMessage());
         // TODO: Potentially send a message to an error queue or log more formally
         return;
      } catch (IllegalArgumentException e) {
         System.err.println("AvailabilityService (Listener): Error parsing room type from DTO: " + e.getMessage());
         // TODO: Potentially send a message to an error queue or log more formally
         return;
      }

      // --- Availability Check Logic (adapted from previous method) ---
      String availabilityMessage = "No rooms of type " + roomType + " are available for the selected dates.";

      List<Room> roomsOfType = roomRepository.findAll().stream()
            .filter(room -> room.getRoomType() == roomType)
            .collect(Collectors.toList());

      if (roomsOfType.isEmpty()) {
         availabilityMessage = "No rooms of type " + roomType + " exist in the hotel.";
      } else {
         for (Room room : roomsOfType) {
            List<Reservation> conflictingReservations = reservationRepository.findAll().stream()
                  .filter(reservation -> reservation.getRoom().getId().equals(room.getId()))
                  .filter(reservation -> !checkOutDate.isBefore(reservation.getCheckInDate()) &&
                        !checkInDate.isAfter(reservation.getCheckOutDate()))
                  .collect(Collectors.toList());

            if (conflictingReservations.isEmpty()) {
               availabilityMessage = String.format(
                     "Room Available!\nNumber: %s, Type: %s, Price: $%.2f\nFor dates: %s to %s",
                     room.getRoomNumber(), room.getRoomType(), room.getPricePerNight(),
                     checkInDate.format(DATE_FORMATTER), checkOutDate.format(DATE_FORMATTER));
               break;
            }
         }
      }
      System.out.println("AvailabilityService (Listener): Validation Result -> " + availabilityMessage.split("\\n")[0]);

      // Now, get existing reservations for the selected room type (as before)
      StringBuilder resultBuilder = new StringBuilder(availabilityMessage);
      List<Reservation> reservationsForType = reservationRepository.findByRoom_RoomType(roomType);

      resultBuilder.append("\n\n--- Existing Reservations for Room Type: ").append(roomType).append(" ---");
      if (reservationsForType.isEmpty()) {
         resultBuilder.append("\nNo reservations found for this room type.");
      } else {
         for (Reservation res : reservationsForType) {
            resultBuilder.append(String.format("\nGuest: %s, Room: %s, Dates: %s to %s",
                  res.getGuestName(),
                  res.getRoom().getRoomNumber(),
                  res.getCheckInDate().format(DATE_FORMATTER),
                  res.getCheckOutDate().format(DATE_FORMATTER)));
         }
      }
      System.out.println("AvailabilityService (Listener): Detailed Info ->\n" + resultBuilder.toString());
      // At this point, the result is only logged. The UI doesn't get it back directly
      // from this listener.
   }

   /*
    * // Commenting out the old synchronous method
    * public String checkAvailability(LocalDate checkInDate, LocalDate
    * checkOutDate, RoomType roomType) {
    * // ... old logic ...
    * }
    */
}