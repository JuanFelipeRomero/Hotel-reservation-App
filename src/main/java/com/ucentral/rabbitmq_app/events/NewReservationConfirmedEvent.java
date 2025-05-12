package com.ucentral.rabbitmq_app.events;

import com.ucentral.rabbitmq_app.dto.FinalBookingDetailsDTO;
import org.springframework.context.ApplicationEvent;

public class NewReservationConfirmedEvent extends ApplicationEvent {

   private final FinalBookingDetailsDTO bookingDetails;

   /**
    * Create a new NewReservationConfirmedEvent.
    * 
    * @param source         the object on which the event initially occurred (never
    *                       {@code null})
    * @param bookingDetails the details of the confirmed booking
    */
   public NewReservationConfirmedEvent(Object source, FinalBookingDetailsDTO bookingDetails) {
      super(source);
      this.bookingDetails = bookingDetails;
   }

   public FinalBookingDetailsDTO getBookingDetails() {
      return bookingDetails;
   }
}