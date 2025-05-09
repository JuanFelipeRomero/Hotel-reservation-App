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
      log.info("NotificationService: Evento de reserva confirmada recibido de RabbitMQ (Cola: {}) -> {}",
            RabbitMQConfig.QUEUE_NOTIFICACIONES, reservation);

      if (reservation == null || reservation.getGuestEmail() == null || reservation.getRoom() == null) {
         log.error("NotificationService: Datos de reserva inválidos recibidos para notificación: {}", reservation);
         return;
      }

      try {
         String guestEmail = reservation.getGuestEmail();
         String subject = "reserva registrada exitosamente";

         long numberOfNights = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
         if (numberOfNights <= 0)
            numberOfNights = 1;

         double pricePerNight = reservation.getRoom().getPricePerNight();
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
               reservation.getGuestName(),
               reservation.getRoom().getRoomNumber(),
               reservation.getRoom().getRoomType().toString(),
               reservation.getCheckInDate().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")),
               reservation.getCheckOutDate().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")),
               numberOfNights,
               pricePerNight,
               totalPrice);

         SimpleMailMessage message = new SimpleMailMessage();
         message.setTo(guestEmail);
         message.setSubject(subject);
         message.setText(body);
         mailSender.send(message);
         log.info("NotificationService: Correo de confirmación enviado exitosamente a {}", guestEmail);

      } catch (MailException e) {
         log.error("NotificationService: Error al enviar correo para Reserva ID {}: {}",
               reservation.getId(), e.getMessage(), e);
      } catch (Exception e) {
         log.error("NotificationService: Error al procesar evento de reserva confirmada para Reserva ID {}: {}",
               reservation.getId(), e.getMessage(), e);
      }
   }
}