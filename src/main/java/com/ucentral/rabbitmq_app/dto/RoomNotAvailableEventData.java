package com.ucentral.rabbitmq_app.dto;

import com.ucentral.rabbitmq_app.model.RoomType;

public class RoomNotAvailableEventData {
   private String checkInDate;
   private String checkOutDate;
   private RoomType roomType;

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