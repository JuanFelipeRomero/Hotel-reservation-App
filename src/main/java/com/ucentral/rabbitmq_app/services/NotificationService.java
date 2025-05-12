package com.ucentral.rabbitmq_app.services;

import com.ucentral.rabbitmq_app.RabbitMQConfig;
import com.ucentral.rabbitmq_app.dto.FinalBookingDetailsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
   public void handleReservationConfirmedEvent(@Payload FinalBookingDetailsDTO bookingDetails) {
      log.info("NotificationService: Detalles de reserva confirmada recibidos de RabbitMQ (Cola: {}) -> {}",
            RabbitMQConfig.QUEUE_NOTIFICACIONES, bookingDetails);

      if (bookingDetails == null || bookingDetails.getGuestEmail() == null || bookingDetails.getRoomNumber() == null) {
         log.error("NotificationService: Datos de reserva inválidos (DTO) recibidos para notificación: {}",
               bookingDetails);
         return;
      }

      try {
         String guestEmail = bookingDetails.getGuestEmail();
         String subject = "reserva registrada exitosamente";

         LocalDate checkInDate = LocalDate.parse(bookingDetails.getCheckInDate(), DATE_FORMATTER);
         LocalDate checkOutDate = LocalDate.parse(bookingDetails.getCheckOutDate(), DATE_FORMATTER);

         long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
         if (numberOfNights <= 0)
            numberOfNights = 1;

         double pricePerNight = bookingDetails.getPricePerNight() != null ? bookingDetails.getPricePerNight() : 0.0;
         double totalPrice = pricePerNight * numberOfNights;

         String body = String.format(
               "🎉 ¡Reserva Confirmada! 🎉\n\n" +
                     "Estimado/a %s,\n\n" +
                     "Nos complace informarle que su reserva ha sido registrada exitosamente.\n" +
                     "A continuación, encontrará los detalles:\n\n" +
                     "🛏️ Habitación Número: %s\n" +
                     "🏷️ Tipo de Habitación: %s\n" +
                     "📅 Fecha de Entrada: %s\n" +
                     "📅 Fecha de Salida: %s\n" +
                     "🌙 Número de Noches: %d\n" +
                     "💵 Precio por Noche: $%.2f\n" +
                     "💰 Precio Total Estimado: $%.2f\n\n" +
                     "¡Gracias por elegirnos! 💖\n" +
                     "Esperamos que disfrute su estadía con nosotros 😊",
               bookingDetails.getGuestName(),
               bookingDetails.getRoomNumber(),
               bookingDetails.getRoomType() != null ? bookingDetails.getRoomType().toString() : "N/A",
               checkInDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")),
               checkOutDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")),
               numberOfNights,
               pricePerNight,
               totalPrice);

         SimpleMailMessage message = new SimpleMailMessage();
         message.setTo(guestEmail);
         message.setSubject(subject);
         message.setText(body);
         mailSender.send(message);
         log.info("NotificationService: Correo de confirmación enviado exitosamente a {}", guestEmail);

      } catch (DateTimeParseException e) {
         log.error("NotificationService: Error parsing dates '{}', '{}' from booking details DTO: {}",
               bookingDetails.getCheckInDate(), bookingDetails.getCheckOutDate(), e.getMessage(), e);
      } catch (MailException e) {
         log.error("NotificationService: Error al enviar correo para DTO {}: {}",
               bookingDetails, e.getMessage(), e);
      } catch (Exception e) {
         log.error("NotificationService: Error al procesar evento de reserva confirmada DTO {}: {}",
               bookingDetails, e.getMessage(), e);
      }
   }
}