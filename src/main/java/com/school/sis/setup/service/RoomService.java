package com.school.sis.setup.service;

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

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<RoomResponse> list(String search, Pageable pageable) {
        String term = search == null ? "" : search;
        return PageResponse.from(roomRepository.findByRoomCodeContainingIgnoreCaseOrRoomNameContainingIgnoreCase(term, term, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public RoomResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public RoomResponse create(RoomRequest request) {
        Room room = new Room();
        apply(room, request);
        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse update(UUID id, RoomRequest request) {
        Room room = find(id);
        apply(room, request);
        return toResponse(room);
    }

    @Transactional
    public RoomResponse updateStatus(UUID id, ActiveStatus status) {
        Room room = find(id);
        room.setStatus(status);
        return toResponse(room);
    }

    Room find(UUID id) {
        return roomRepository.findById(id).orElseThrow(() -> new NotFoundException("Room not found"));
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
