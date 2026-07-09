package com.school.sis.setup.repository;

import com.school.sis.setup.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    Page<Room> findByRoomCodeContainingIgnoreCaseOrRoomNameContainingIgnoreCase(String code, String name, Pageable pageable);
}
