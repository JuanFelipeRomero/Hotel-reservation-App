package com.ucentral.rabbitmq_app.services;

import com.ucentral.rabbitmq_app.RabbitMQConfig; // For queue name constant
import com.ucentral.rabbitmq_app.dto.AvailabilityRequestDTO;
import com.ucentral.rabbitmq_app.dto.RoomAvailableEventData; // Import for event data
import com.ucentral.rabbitmq_app.dto.RoomNotAvailableEventData; // Added import
import com.ucentral.rabbitmq_app.model.Reservation;
import com.ucentral.rabbitmq_app.model.Room;
import com.ucentral.rabbitmq_app.model.RoomType;
import com.ucentral.rabbitmq_app.repository.ReservationRepository;
import com.ucentral.rabbitmq_app.repository.RoomRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher; // For publishing events
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
   private final ApplicationEventPublisher eventPublisher; // Added
   private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

   @Autowired
   public AvailabilityService(RoomRepository roomRepository, ReservationRepository reservationRepository,
         ApplicationEventPublisher eventPublisher) {
      this.roomRepository = roomRepository;
      this.reservationRepository = reservationRepository;
      this.eventPublisher = eventPublisher; // Added
   }

   @RabbitListener(queues = RabbitMQConfig.QUEUE_RESERVACIONES_PARA_DISPONIBILIDAD)
   public void handleAvailabilityRequest(@Payload AvailabilityRequestDTO requestDTO) {
      System.out.println("Service Listener: Data received from RabbitMQ (Queue: "
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
         System.err.println("Service Listener: Error parsing DTO: " + e.getMessage());
         return;
      }

      String availabilityMessage = "No rooms of type " + roomType + " are available for the selected dates.";
      boolean wasRoomAvailable = false; // Flag to track if we found one

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
               availableRoom = room; // Capture the available room
               wasRoomAvailable = true; // Set flag
               availabilityMessage = String.format(
                     "Room Available! Number: %s, Type: %s, Price: $%.2f For dates: %s to %s",
                     room.getRoomNumber(), room.getRoomType(), room.getPricePerNight(),
                     checkInDate.format(DATE_FORMATTER), checkOutDate.format(DATE_FORMATTER));
               break;
            }
         }
      }
      System.out.println("AvailabilityService (Listener): Validation Result -> " + availabilityMessage);

      if (wasRoomAvailable && availableRoom != null) {
         // Publish an event with the available room details
         RoomAvailableEventData eventData = new RoomAvailableEventData(
               availableRoom.getId(),
               availableRoom.getRoomNumber(),
               availableRoom.getRoomType(),
               availableRoom.getPricePerNight(),
               checkInDate.format(DATE_FORMATTER),
               checkOutDate.format(DATE_FORMATTER));
         eventPublisher.publishEvent(eventData); // Publishing the data directly as the event object
         System.out.println("Service Listener: Published RoomAvailableEvent with data: " + eventData);
      } else {
         // Publish an event indicating no room was available
         RoomNotAvailableEventData notAvailableEventData = new RoomNotAvailableEventData(
               checkInDate.format(DATE_FORMATTER),
               checkOutDate.format(DATE_FORMATTER),
               roomType);
         eventPublisher.publishEvent(notAvailableEventData);
         System.out.println("Service Listener: No available room found, published RoomNotAvailableEvent.");
      }

      // Original detailed logging for console (can be removed or kept)
      StringBuilder resultBuilder = new StringBuilder(availabilityMessage);
      List<Reservation> reservationsForType = reservationRepository.findByRoom_RoomType(roomType);
      resultBuilder.append("\n\n--- Existing Reservations for Room Type: ").append(roomType).append(" ---");
      if (reservationsForType.isEmpty()) {
         resultBuilder.append("\nNo reservations found for this room type.");
      } else {
         for (Reservation res : reservationsForType) {
            resultBuilder.append(String.format("\nGuest: %s, Room: %s, Dates: %s to %s",
                  res.getGuestName(), res.getRoom().getRoomNumber(),
                  res.getCheckInDate().format(DATE_FORMATTER), res.getCheckOutDate().format(DATE_FORMATTER)));
         }
      }
      System.out.println("AvailabilityService (Listener): Detailed Info Log ->\n" + resultBuilder.toString());
   }

   /*
    * // Commenting out the old synchronous method
    * public String checkAvailability(LocalDate checkInDate, LocalDate
    * checkOutDate, RoomType roomType) {
    * // ... old logic ...
    * }
    */
}