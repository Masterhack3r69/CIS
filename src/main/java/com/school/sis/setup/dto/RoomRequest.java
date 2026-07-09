package com.school.sis.setup.dto;

import com.school.sis.setup.entity.ActiveStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RoomRequest(
        @NotBlank String roomCode,
        @NotBlank String roomName,
        @Min(0) Integer capacity,
        ActiveStatus status
) {
}
