package com.ucentral.rabbitmq_app.dto;

public class AvailabilityRequestDTO {
   private String checkInDate; // yyyy-MM-dd format
   private String checkOutDate; // yyyy-MM-dd format
   private String roomType; // "SENCILLA", "DOBLE", "SUITE"

   public AvailabilityRequestDTO() {
   }

   public AvailabilityRequestDTO(String checkInDate, String checkOutDate, String roomType) {
      this.checkInDate = checkInDate;
      this.checkOutDate = checkOutDate;
      this.roomType = roomType;
   }

   public String getCheckInDate() {
      return checkInDate;
   }

   public void setCheckInDate(String checkInDate) {
      this.checkInDate = checkInDate;
   }

   public String getCheckOutDate() {
      return checkOutDate;
   }

   public void setCheckOutDate(String checkOutDate) {
      this.checkOutDate = checkOutDate;
   }

   public String getRoomType() {
      return roomType;
   }

   public void setRoomType(String roomType) {
      this.roomType = roomType;
   }

   @Override
   public String toString() {
      return "AvailabilityRequestDTO{" +
            "checkInDate='" + checkInDate + '\'' +
            ", checkOutDate='" + checkOutDate + '\'' +
            ", roomType='" + roomType + '\'' +
            '}';
   }
}