package com.school.sis.fee.controller;

import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.fee.dto.FeeItemRequest;
import com.school.sis.fee.dto.FeeItemResponse;
import com.school.sis.fee.dto.FeeItemSummaryResponse;
import com.school.sis.fee.dto.FeeSearchCriteria;
import com.school.sis.fee.entity.FeeCategory;
import com.school.sis.fee.service.FeeService;
import com.school.sis.setup.entity.ActiveStatus;
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
@RequestMapping("/api/v1/fees")
public class FeeController {

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<PageResponse<FeeItemSummaryResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) FeeCategory category,
            @RequestParam(required = false) ActiveStatus status,
            Pageable pageable
    ) {
        return ApiResponse.success("Fees retrieved", feeService.list(new FeeSearchCriteria(search, category, status), pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<FeeItemResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("Fee retrieved", feeService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<FeeItemResponse> create(@Valid @RequestBody FeeItemRequest request) {
        return ApiResponse.success("Fee created", feeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<FeeItemResponse> update(@PathVariable UUID id, @Valid @RequestBody FeeItemRequest request) {
        return ApiResponse.success("Fee updated", feeService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<FeeItemResponse> updateStatus(@PathVariable UUID id, @RequestBody Map<String, ActiveStatus> request) {
        return ApiResponse.success("Fee status updated", feeService.updateStatus(id, request.get("status")));
    }
}
