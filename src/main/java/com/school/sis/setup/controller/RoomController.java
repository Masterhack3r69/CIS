package com.school.sis.setup.controller;

import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.RoomRequest;
import com.school.sis.setup.dto.RoomResponse;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")
    public ApiResponse<PageResponse<RoomResponse>> list(@RequestParam(required = false) String search, Pageable pageable) {
        return ApiResponse.success("Rooms retrieved", roomService.list(search, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")
    public ApiResponse<RoomResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("Room retrieved", roomService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<RoomResponse> create(@Valid @RequestBody RoomRequest request) {
        return ApiResponse.success("Room created", roomService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<RoomResponse> update(@PathVariable UUID id, @Valid @RequestBody RoomRequest request) {
        return ApiResponse.success("Room updated", roomService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<RoomResponse> updateStatus(@PathVariable UUID id, @RequestBody Map<String, ActiveStatus> request) {
        return ApiResponse.success("Room status updated", roomService.updateStatus(id, request.get("status")));
    }
}
