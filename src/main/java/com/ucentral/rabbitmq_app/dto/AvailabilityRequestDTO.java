package com.ucentral.rabbitmq_app.dto;

// No need for Lombok or explicit getters/setters if using Jackson for serialization
// and fields are public or if record is used (Java 14+).
// For broader compatibility and typical DTO patterns, getters/setters are often included.
// Using a simple class with public fields for brevity with Jackson.
// Or, for better encapsulation with Jackson, use private fields and public getters/setters.
// Let's use private fields and getters/setters for good practice.

public class AvailabilityRequestDTO {
   private String checkInDate; // Expecting yyyy-MM-dd format
   private String checkOutDate; // Expecting yyyy-MM-dd format
   private String roomType; // e.g., "SENCILLA", "DOBLE", "SUITE"

   // Jackson needs a no-arg constructor for deserialization
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