package com.school.sis.setup.dto;

import com.school.sis.setup.entity.ActiveStatus;

import java.util.UUID;

public record RoomResponse(
        UUID id,
        String roomCode,
        String roomName,
        Integer capacity,
        ActiveStatus status
) {
}
