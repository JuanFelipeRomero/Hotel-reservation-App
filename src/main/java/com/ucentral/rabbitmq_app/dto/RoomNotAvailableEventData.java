package com.ucentral.rabbitmq_app.dto;

import com.ucentral.rabbitmq_app.model.RoomType;

// Event data published when no room is available for the request
public class RoomNotAvailableEventData {
   private String checkInDate; // Original requested date
   private String checkOutDate; // Original requested date
   private RoomType roomType; // Original requested type

   public RoomNotAvailableEventData(String checkInDate, String checkOutDate, RoomType roomType) {
      this.checkInDate = checkInDate;
      this.checkOutDate = checkOutDate;
      this.roomType = roomType;
   }

   // Getters
   public String getCheckInDate() {
      return checkInDate;
   }

   public String getCheckOutDate() {
      return checkOutDate;
   }

   public RoomType getRoomType() {
      return roomType;
   }

   @Override
   public String toString() {
      return "RoomNotAvailableEventData{" +
            "checkInDate='" + checkInDate + '\'' +
            ", checkOutDate='" + checkOutDate + '\'' +
            ", roomType=" + roomType +
            '}';
   }
}