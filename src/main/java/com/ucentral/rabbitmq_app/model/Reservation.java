package com.ucentral.rabbitmq_app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import java.time.LocalDate;

@Entity
@Table(name = "reservations")
public class Reservation { // Consider renaming class to Reservation

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne
   @JoinColumn(name = "room_id", nullable = false)
   private Room room;

   @Column(name = "guest_name", nullable = false)
   private String guestName;

   @Column(name = "guest_id", nullable = false)
   private String guestId;

   @Column(name = "check_in_date", nullable = false)
   private LocalDate checkInDate;

   @Column(name = "check_out_date", nullable = false)
   private LocalDate checkOutDate;

   // Constructors
   public Reservation() {
   }

   public Reservation(Room room, String guestName, String guestId, LocalDate checkInDate, LocalDate checkOutDate) {
      this.room = room;
      this.guestName = guestName;
      this.guestId = guestId;
      this.checkInDate = checkInDate;
      this.checkOutDate = checkOutDate;
   }

   // Getters and Setters
   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public Room getRoom() {
      return room;
   }

   public void setRoom(Room room) {
      this.room = room;
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

   public LocalDate getCheckInDate() {
      return checkInDate;
   }

   public void setCheckInDate(LocalDate checkInDate) {
      this.checkInDate = checkInDate;
   }

   public LocalDate getCheckOutDate() {
      return checkOutDate;
   }

   public void setCheckOutDate(LocalDate checkOutDate) {
      this.checkOutDate = checkOutDate;
   }

   // toString() method (optional, but useful for logging/debugging)
   @Override
   public String toString() {
      return "Reservation{" +
            "id=" + id +
            ", room=" + (room != null ? room.getId() : null) +
            ", guestName='" + guestName + "'" +
            ", guestId='" + guestId + "'" +
            ", checkInDate=" + checkInDate +
            ", checkOutDate=" + checkOutDate +
            '}';
   }
}
