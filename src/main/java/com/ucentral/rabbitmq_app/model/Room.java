package com.ucentral.rabbitmq_app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "rooms")
public class Room {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "room_number", unique = true, nullable = false)
   private String roomNumber;

   @Enumerated(EnumType.STRING)
   @Column(name = "room_type", nullable = false)
   private RoomType roomType;

   @Column(name = "price_per_night", nullable = false)
   private double pricePerNight;

   // Constructors
   public Room() {
   }

   public Room(String roomNumber, RoomType roomType, double pricePerNight) {
      this.roomNumber = roomNumber;
      this.roomType = roomType;
      this.pricePerNight = pricePerNight;
   }

   // Getters and Setters
   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
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

   public double getPricePerNight() {
      return pricePerNight;
   }

   public void setPricePerNight(double pricePerNight) {
      this.pricePerNight = pricePerNight;
   }

   // toString() method (optional, but useful for logging/debugging)
   @Override
   public String toString() {
      return "Room{" +
            "id=" + id +
            ", roomNumber='" + roomNumber + '\'' +
            ", roomType=" + roomType +
            ", pricePerNight=" + pricePerNight +
            '}';
   }
}