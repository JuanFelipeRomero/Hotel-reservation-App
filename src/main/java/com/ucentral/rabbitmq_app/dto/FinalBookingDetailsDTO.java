package com.ucentral.rabbitmq_app.dto;

import com.ucentral.rabbitmq_app.model.RoomType;

public class FinalBookingDetailsDTO {
   // Details from the originally found available room
   private Long roomId;
   private String roomNumber;
   private RoomType roomType;
   private Double pricePerNight;
   private String checkInDate; // yyyy-MM-dd
   private String checkOutDate; // yyyy-MM-dd

   // Details from the new booking confirmation form
   private String guestName;
   private String guestId;
   private String guestEmail;

   // No-arg constructor for Jackson
   public FinalBookingDetailsDTO() {
   }

   public FinalBookingDetailsDTO(Long roomId, String roomNumber, RoomType roomType, Double pricePerNight,
         String checkInDate, String checkOutDate,
         String guestName, String guestId, String guestEmail) {
      this.roomId = roomId;
      this.roomNumber = roomNumber;
      this.roomType = roomType;
      this.pricePerNight = pricePerNight;
      this.checkInDate = checkInDate;
      this.checkOutDate = checkOutDate;
      this.guestName = guestName;
      this.guestId = guestId;
      this.guestEmail = guestEmail;
   }

   // Getters and Setters
   public Long getRoomId() {
      return roomId;
   }

   public void setRoomId(Long roomId) {
      this.roomId = roomId;
   }

   public String getRoomNumber() {
      return roomNumber;
   }

   public void setRoomNumber(String roomNumber) {
      this.roomNumber = roomNumber;
   }

   public RoomType getRoomType() {
      return roomType;
   }

   public void setRoomType(RoomType roomType) {
      this.roomType = roomType;
   }

   public Double getPricePerNight() {
      return pricePerNight;
   }

   public void setPricePerNight(Double pricePerNight) {
      this.pricePerNight = pricePerNight;
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

   public String getGuestName() {
      return guestName;
   }

   public void setGuestName(String guestName) {
      this.guestName = guestName;
   }

   public String getGuestId() {
      return guestId;
   }

   public void setGuestId(String guestId) {
      this.guestId = guestId;
   }

   public String getGuestEmail() {
      return guestEmail;
   }

   public void setGuestEmail(String guestEmail) {
      this.guestEmail = guestEmail;
   }

   @Override
   public String toString() {
      return "FinalBookingDetailsDTO{" +
            "roomId=" + roomId +
            ", roomNumber='" + roomNumber + '\'' +
            ", roomType=" + roomType +
            ", pricePerNight=" + pricePerNight +
            ", checkInDate='" + checkInDate + '\'' +
            ", checkOutDate='" + checkOutDate + '\'' +
            ", guestName='" + guestName + '\'' +
            ", guestId='" + guestId + '\'' +
            ", guestEmail='" + guestEmail + '\'' +
            '}';
   }
}