package com.ucentral.rabbitmq_app.services;

import com.ucentral.rabbitmq_app.RabbitMQConfig;
import com.ucentral.rabbitmq_app.dto.FinalBookingDetailsDTO;
import com.ucentral.rabbitmq_app.events.NewReservationConfirmedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class AdminReservationListener {

   private final ApplicationEventPublisher eventPublisher;

   @Autowired
   public AdminReservationListener(ApplicationEventPublisher eventPublisher) {
      this.eventPublisher = eventPublisher;
   }

   @RabbitListener(queues = RabbitMQConfig.QUEUE_ADMIN)
   public void handleConfirmedReservation(@Payload FinalBookingDetailsDTO bookingDetails) {
      System.out.println("AdminListener: Received confirmed reservation from QUEUE_ADMIN: " + bookingDetails);

      try {
         NewReservationConfirmedEvent event = new NewReservationConfirmedEvent(this, bookingDetails);
         eventPublisher.publishEvent(event);
         System.out.println("AdminListener: Published NewReservationConfirmedEvent for UI.");
      } catch (Exception e) {
         System.err.println("AdminListener: Error publishing NewReservationConfirmedEvent: " + e.getMessage());
         e.printStackTrace();
      }
   }
}