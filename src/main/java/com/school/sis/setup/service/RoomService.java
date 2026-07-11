package com.school.sis.setup.service;
import com.school.sis.audit.AuditModule;
import com.school.sis.audit.service.AuditService;

import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.RoomRequest;
import com.school.sis.setup.dto.RoomResponse;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.Room;
import com.school.sis.setup.repository.RoomRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final AuditService auditService;

    public RoomService(RoomRepository roomRepository, AuditService auditService) {
        this.roomRepository = roomRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<RoomResponse> list(String search, Pageable pageable) {
        String term = search == null ? "" : search;
        return PageResponse.from(roomRepository
                .findByRoomCodeContainingIgnoreCaseOrRoomNameContainingIgnoreCase(term, term, pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public RoomResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public RoomResponse create(RoomRequest request) {
        Room room = new Room();
        apply(room, request);
        RoomResponse response = toResponse(roomRepository.save(room)); auditService.log("ROOM_CREATED", AuditModule.ACADEMIC_SETUP, "Room", response.id(), null, response); return response;
    }

    @Transactional
    public RoomResponse update(UUID id, RoomRequest request) {
        Room room = find(id);
        RoomResponse before = toResponse(room);
        apply(room, request);
        RoomResponse after = toResponse(room); auditService.log("ROOM_UPDATED", AuditModule.ACADEMIC_SETUP, "Room", id, before, after); return after;
    }

    @Transactional
    public RoomResponse updateStatus(UUID id, ActiveStatus status) {
        Room room = find(id);
        ActiveStatus before = room.getStatus();
        room.setStatus(status);
        RoomResponse response = toResponse(room); auditService.log("ROOM_STATUS_UPDATED", AuditModule.ACADEMIC_SETUP, "Room", id, java.util.Map.of("status", before), java.util.Map.of("status", status)); return response;
    }

    private Room find(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found"));
    }

    private void apply(Room room, RoomRequest request) {
        room.setRoomCode(request.roomCode());
        room.setRoomName(request.roomName());
        room.setCapacity(request.capacity());
        room.setStatus(request.status() == null ? ActiveStatus.ACTIVE : request.status());
    }

    private RoomResponse toResponse(Room room) {
        return new RoomResponse(room.getId(), room.getRoomCode(), room.getRoomName(), room.getCapacity(), room.getStatus());
    }
}
