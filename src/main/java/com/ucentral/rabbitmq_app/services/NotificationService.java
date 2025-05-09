package com.ucentral.rabbitmq_app.services;

import com.ucentral.rabbitmq_app.RabbitMQConfig;
import com.ucentral.rabbitmq_app.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationService {

   private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
   private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

   private final JavaMailSender mailSender;

   @Autowired
   public NotificationService(JavaMailSender mailSender) {
      this.mailSender = mailSender;
   }

   @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICACIONES)
   public void handleReservationConfirmedEvent(@Payload Reservation reservation) {
      log.info("NotificationService: Received confirmed reservation event from RabbitMQ (Queue: {}) -> {}",
            RabbitMQConfig.QUEUE_NOTIFICACIONES, reservation);

      if (reservation == null || reservation.getGuestEmail() == null || reservation.getRoom() == null) {
         log.error("NotificationService: Received invalid reservation data for notification: {}", reservation);
         return;
      }

      try {
         String guestEmail = reservation.getGuestEmail();
         String subject = "reserva registrada exitosamente";

         long numberOfNights = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
         if (numberOfNights <= 0)
            numberOfNights = 1; // Assuming at least a one-night stay for price calculation if dates are same

         double pricePerNight = reservation.getRoom().getPricePerNight();
         double totalPrice = pricePerNight * numberOfNights;

         String body = String.format(
               "Estimado/a %s,\n\n" +
                     "Su reserva ha sido registrada exitosamente.\n\n" +
                     "Detalles de la reserva:\n" +
                     "  Habitación Número: %s\n" +
                     "  Tipo de Habitación: %s\n" +
                     "  Fecha de Entrada: %s\n" +
                     "  Fecha de Salida: %s\n" +
                     "  Número de Noches: %d\n" +
                     "  Precio por Noche: $%.2f\n" +
                     "  Precio Total Estimado: $%.2f\n\n" +
                     "Gracias por su preferencia.",
               reservation.getGuestName(),
               reservation.getRoom().getRoomNumber(),
               reservation.getRoom().getRoomType().toString(), // Assuming RoomType has a reasonable toString()
               reservation.getCheckInDate().format(DATE_FORMATTER),
               reservation.getCheckOutDate().format(DATE_FORMATTER),
               numberOfNights,
               pricePerNight,
               totalPrice);

         // Send actual email
         SimpleMailMessage message = new SimpleMailMessage();
         message.setTo(guestEmail);
         message.setSubject(subject);
         message.setText(body);
         // Consider setting a 'from' address if needed, e.g.,
         // message.setFrom("noreply@example.com");
         // This might be configured globally via spring.mail.from in
         // application.properties

         mailSender.send(message);
         log.info("NotificationService: Confirmation email successfully sent to {}", guestEmail);

      } catch (MailException e) {
         log.error("NotificationService: Error sending email for Reservation ID {}: {}",
               reservation.getId(), e.getMessage(), e);
      } catch (Exception e) {
         log.error("NotificationService: Error processing confirmed reservation event for Reservation ID {}: {}",
               reservation.getId(), e.getMessage(), e);
      }
   }
}