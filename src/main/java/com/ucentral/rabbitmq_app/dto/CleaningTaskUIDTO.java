package com.ucentral.rabbitmq_app.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CleaningTaskUIDTO {
   private Long reservationId;
   private Long roomId;
   private String roomNumber;
   private String checkOutDate;
   private String status;

   private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

   public CleaningTaskUIDTO() {
   }

   public CleaningTaskUIDTO(Long reservationId, Long roomId, String roomNumber, LocalDate checkOutDate) {
      this.reservationId = reservationId;
      this.roomId = roomId;
      this.roomNumber = roomNumber;
      this.checkOutDate = checkOutDate.format(DATE_FORMATTER);
      this.status = "PENDING"; // Default status
   }

   public Long getReservationId() {
      return reservationId;
   }

   public Long getRoomId() {
      return roomId;
   }

   public String getRoomNumber() {
      return roomNumber;
   }

   public String getCheckOutDate() {
      return checkOutDate;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   @Override
   public String toString() {
      return String.format("Room %s (Res ID: %d) - Checkout: %s [%s]",
            roomNumber, reservationId, checkOutDate, status);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      CleaningTaskUIDTO that = (CleaningTaskUIDTO) o;
      return reservationId.equals(that.reservationId);
   }

   @Override
   public int hashCode() {
      return reservationId.hashCode();
   }
}