package com.ucentral.rabbitmq_app.repository;

import com.ucentral.rabbitmq_app.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
}