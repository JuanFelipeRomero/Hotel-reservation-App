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
      private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

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
      @Transactional
      public void handleReservationRequest(@Payload FinalBookingDetailsDTO bookingDetails) {
            log.info("Servicio de Registro Listener: Detalles finales de reserva recibidos de RabbitMQ (Cola: {}) -> {}",
                        RabbitMQConfig.QUEUE_PROCESSED_DETAILS, bookingDetails);

            try {
                  // 1. Parse dates
                  LocalDate checkInDate = LocalDate.parse(bookingDetails.getCheckInDate(), DATE_FORMATTER);
                  LocalDate checkOutDate = LocalDate.parse(bookingDetails.getCheckOutDate(), DATE_FORMATTER);

                  // 2. Fetch the Room entity
                  Optional<Room> roomOptional = roomRepository.findById(bookingDetails.getRoomId());
                  if (roomOptional.isEmpty()) {
                        log.error(
                                    "No se pudo encontrar Habitación con ID {} especificado en detalles de reserva. Abortando reserva.",
                                    bookingDetails.getRoomId());
                        return;
                  }
                  Room room = roomOptional.get();

                  Reservation newReservation = new Reservation(
                              room,
                              bookingDetails.getGuestName(),
                              bookingDetails.getGuestId(),
                              bookingDetails.getGuestEmail(),
                              checkInDate,
                              checkOutDate);

                  // 4. Save the reservation
                  Reservation savedReservation = reservationRepository.save(newReservation);
                  log.info("Nueva reserva guardada exitosamente con ID: {}",
                              savedReservation.getId());

                  // 5. Create DTO from saved entity to publish
                  FinalBookingDetailsDTO confirmationDto = new FinalBookingDetailsDTO(
                              savedReservation.getRoom().getId(),
                              savedReservation.getRoom().getRoomNumber(),
                              savedReservation.getRoom().getRoomType(),
                              savedReservation.getRoom().getPricePerNight(),
                              savedReservation.getCheckInDate().format(DATE_FORMATTER),
                              savedReservation.getCheckOutDate().format(DATE_FORMATTER),
                              savedReservation.getGuestName(),
                              savedReservation.getGuestId(),
                              savedReservation.getGuestEmail());

                  try {
                        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_RESERVA_CONFIRMADA,
                                    "", // Routing key is ignored for Fanout exchanges
                                    confirmationDto); // Send the DTO
                        log.info("Evento de reserva confirmada (DTO) publicado a Intercambio '{}': {}",
                                    RabbitMQConfig.EXCHANGE_RESERVA_CONFIRMADA, confirmationDto);
                  } catch (Exception e) {
                        log.error(
                                    "Error al publicar evento de reserva confirmada (DTO) para Reserva ID {}: {}",
                                    savedReservation.getId(), e.getMessage(), e);
                  }

            } catch (DateTimeParseException e) {
                  log.error("Error al parsear fechas desde DTO de detalles de reserva: {}. Mensaje: {}",
                              bookingDetails, e.getMessage());
            } catch (IllegalArgumentException e) {
                  log.error("Error al procesar DTO de detalles de reserva: {}. Mensaje: {}",
                              bookingDetails,
                              e.getMessage());
            } catch (Exception e) {
                  log.error(
                              "Error inesperado al guardar reserva para DTO de detalles de reserva: {}. Error: {}",
                              bookingDetails, e.getMessage(), e);
            }
      }
}