package com.ucentral.rabbitmq_app.dto;

public class ReservationRequestDetailsDTO {
   private Long roomId;
   private String roomNumber;
   // private String roomType; // roomType is known by the consumer of original
   // request if needed
   private Double pricePerNight; // Price at the time of availability check
   private String checkInDate; // Expecting yyyy-MM-dd format
   private String checkOutDate; // Expecting yyyy-MM-dd format
   private String guestName; // Placeholder for now

   // No-arg constructor for Jackson
   public ReservationRequestDetailsDTO() {
   }

   public ReservationRequestDetailsDTO(Long roomId, String roomNumber, Double pricePerNight, String checkInDate,
         String checkOutDate, String guestName) {
      this.roomId = roomId;
      this.roomNumber = roomNumber;
      this.pricePerNight = pricePerNight;
      this.checkInDate = checkInDate;
      this.checkOutDate = checkOutDate;
      this.guestName = guestName;
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

   @Override
   public String toString() {
      return "ReservationRequestDetailsDTO{" +
            "roomId=" + roomId +
            ", roomNumber='" + roomNumber + '\'' +
            ", pricePerNight=" + pricePerNight +
            ", checkInDate='" + checkInDate + '\'' +
            ", checkOutDate='" + checkOutDate + '\'' +
            ", guestName='" + guestName + '\'' +
            '}';
   }
}