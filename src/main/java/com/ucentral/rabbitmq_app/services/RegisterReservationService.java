package com.ucentral.rabbitmq_app.services;

import com.ucentral.rabbitmq_app.RabbitMQConfig;
import com.ucentral.rabbitmq_app.dto.FinalBookingDetailsDTO;
import com.ucentral.rabbitmq_app.model.Reservation;
import com.ucentral.rabbitmq_app.model.Room;
import com.ucentral.rabbitmq_app.repository.ReservationRepository;
import com.ucentral.rabbitmq_app.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
public class RegisterReservationService {

   private static final Logger log = LoggerFactory.getLogger(RegisterReservationService.class);
   private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd

   private final ReservationRepository reservationRepository;
   private final RoomRepository roomRepository;
   private final RabbitTemplate rabbitTemplate;

   @Autowired
   public RegisterReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository,
         RabbitTemplate rabbitTemplate) {
      this.reservationRepository = reservationRepository;
      this.roomRepository = roomRepository;
      this.rabbitTemplate = rabbitTemplate;
   }

   @RabbitListener(queues = RabbitMQConfig.QUEUE_PROCESSED_DETAILS)
   @Transactional // Make the operation transactional
   public void handleReservationRequest(@Payload FinalBookingDetailsDTO bookingDetails) {
      log.info("Register Service Listener: Received final booking details from RabbitMQ (Queue: {}) -> {}",
            RabbitMQConfig.QUEUE_PROCESSED_DETAILS, bookingDetails);

      try {
         // 1. Parse dates
         LocalDate checkInDate = LocalDate.parse(bookingDetails.getCheckInDate(), DATE_FORMATTER);
         LocalDate checkOutDate = LocalDate.parse(bookingDetails.getCheckOutDate(), DATE_FORMATTER);

         // 2. Fetch the Room entity
         Optional<Room> roomOptional = roomRepository.findById(bookingDetails.getRoomId());
         if (roomOptional.isEmpty()) {
            log.error(
                  "Register Service Listener: Could not find Room with ID {} specified in booking details. Aborting reservation.",
                  bookingDetails.getRoomId());
            // TODO: Handle error - maybe send to DLQ or notify originator
            return;
         }
         Room room = roomOptional.get();

         // Optional: Double check room details match DTO? (e.g., room number, price)
         // if (!room.getRoomNumber().equals(bookingDetails.getRoomNumber())) { ... log
         // warning ... }

         // 3. Create Reservation entity
         Reservation newReservation = new Reservation(
               room,
               bookingDetails.getGuestName(),
               bookingDetails.getGuestId(),
               bookingDetails.getGuestEmail(),
               checkInDate,
               checkOutDate);

         // 4. Save the reservation
         Reservation savedReservation = reservationRepository.save(newReservation);
         log.info("Register Service Listener: Successfully saved new reservation with ID: {}",
               savedReservation.getId());

         // After successful save, publish to fanout exchange
         try {
            // Sending the saved Reservation entity itself. Ensure it's serializable.
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_RESERVA_CONFIRMADA,
                  "", // Routing key is ignored by Fanout exchanges
                  savedReservation);
            log.info("Register Service Listener: Published confirmed reservation event to Exchange '{}': {}",
                  RabbitMQConfig.EXCHANGE_RESERVA_CONFIRMADA, savedReservation);
         } catch (Exception e) {
            log.error(
                  "Register Service Listener: Error publishing confirmed reservation event for Reservation ID {}: {}",
                  savedReservation.getId(), e.getMessage(), e);
            // The original reservation is saved, but event publishing failed.
            // Consider compensation logic or more robust event publishing (e.g.,
            // transactional outbox pattern)
         }

      } catch (DateTimeParseException e) {
         log.error("Register Service Listener: Error parsing dates from booking details DTO: {}. Message: {}",
               bookingDetails, e.getMessage());
         // TODO: Handle error - DLQ?
      } catch (IllegalArgumentException e) {
         log.error("Register Service Listener: Error processing booking details DTO: {}. Message: {}", bookingDetails,
               e.getMessage());
         // TODO: Handle error - DLQ?
      } catch (Exception e) { // Catch broader exceptions during DB interaction
         log.error(
               "Register Service Listener: Unexpected error saving reservation for booking details DTO: {}. Error: {}",
               bookingDetails, e.getMessage(), e);
         // TODO: Handle error - DLQ?
         // Consider re-throwing a specific runtime exception if transaction should
         // definitely rollback
      }
   }
}