package com.ucentral.rabbitmq_app.repository;

import com.ucentral.rabbitmq_app.model.Reservation;
import com.ucentral.rabbitmq_app.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
   // Example custom query methods:
   // List<Reservation> findByGuestName(String guestName);
   // List<Reservation> findByCheckInDateBetween(LocalDate startDate, LocalDate
   // endDate);
   // List<Reservation> findByRoomId(Long roomId);

   List<Reservation> findByRoom_RoomType(RoomType roomType);
}