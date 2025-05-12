package com.ucentral.rabbitmq_app.repository;

import com.ucentral.rabbitmq_app.model.Reservation;
import com.ucentral.rabbitmq_app.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
      List<Reservation> findByRoom_RoomType(RoomType roomType);

      @Query("SELECT r FROM Reservation r WHERE r.room.id = :roomId AND r.checkInDate < :checkOutDate AND r.checkOutDate > :checkInDate")
      List<Reservation> findConflictingReservations(
                  @Param("roomId") Long roomId,
                  @Param("checkInDate") LocalDate checkInDate,
                  @Param("checkOutDate") LocalDate checkOutDate);

      @Query("SELECT r FROM Reservation r WHERE r.checkInDate <= :date AND r.checkOutDate > :date")
      List<Reservation> findActiveReservationsForDate(@Param("date") LocalDate date);
}