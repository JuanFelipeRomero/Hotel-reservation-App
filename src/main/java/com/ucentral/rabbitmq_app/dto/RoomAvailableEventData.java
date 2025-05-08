package com.ucentral.rabbitmq_app.dto;

import com.ucentral.rabbitmq_app.model.RoomType;

// This DTO carries data from AvailabilityService to the UI event listener
public class RoomAvailableEventData {
   private Long roomId;
   private String roomNumber;
   private RoomType roomType;
   private Double pricePerNight;
   private String checkInDate; // yyyy-MM-dd
   private String checkOutDate; // yyyy-MM-dd

   public RoomAvailableEventData(Long roomId, String roomNumber, RoomType roomType, Double pricePerNight,
         String checkInDate, String checkOutDate) {
      this.roomId = roomId;
      this.roomNumber = roomNumber;
      this.roomType = roomType;
      this.pricePerNight = pricePerNight;
      this.checkInDate = checkInDate;
      this.checkOutDate = checkOutDate;
   }

   // Getters
   public Long getRoomId() {
      return roomId;
   }

   public String getRoomNumber() {
      return roomNumber;
   }

   public RoomType getRoomType() {
      return roomType;
   }

   public Double getPricePerNight() {
      return pricePerNight;
   }

   public String getCheckInDate() {
      return checkInDate;
   }

   public String getCheckOutDate() {
      return checkOutDate;
   }

   @Override
   public String toString() {
      return "RoomAvailableEventData{" +
            "roomId=" + roomId +
            ", roomNumber='" + roomNumber + '\'' +
            ", roomType=" + roomType +
            ", pricePerNight=" + pricePerNight +
            ", checkInDate='" + checkInDate + '\'' +
            ", checkOutDate='" + checkOutDate + '\'' +
            '}';
   }
}