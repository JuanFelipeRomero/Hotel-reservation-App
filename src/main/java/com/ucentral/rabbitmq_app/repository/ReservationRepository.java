package com.ucentral.rabbitmq_app.repository;

import com.ucentral.rabbitmq_app.model.Reservation;
import com.ucentral.rabbitmq_app.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
   List<Reservation> findByRoom_RoomType(RoomType roomType);
}