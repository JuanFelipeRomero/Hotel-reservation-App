package com.ucentral.rabbitmq_app.dto;

import com.ucentral.rabbitmq_app.model.RoomType;

public class RoomAvailableEventData {
   private Long roomId;
   private String roomNumber;
   private RoomType roomType;
   private Double pricePerNight;
   private String checkInDate;
   private String checkOutDate;

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