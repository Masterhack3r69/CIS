package com.school.sis.setup.entity;

import com.school.sis.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "rooms")
public class Room extends AuditableEntity {
    @Id
    private UUID id;

    @Column(name = "room_code", nullable = false, unique = true, length = 40)
    private String roomCode;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActiveStatus status = ActiveStatus.ACTIVE;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public ActiveStatus getStatus() { return status; }
    public void setStatus(ActiveStatus status) { this.status = status; }
}
