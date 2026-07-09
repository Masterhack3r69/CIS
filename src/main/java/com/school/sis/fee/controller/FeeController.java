package com.school.sis.fee.controller;

import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.fee.dto.FeeRequest;
import com.school.sis.fee.dto.FeeResponse;
import com.school.sis.fee.dto.FeeStatusRequest;
import com.school.sis.fee.service.FeeService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fees")
public class FeeController {

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FEE_VIEW')")
    public ApiResponse<PageResponse<FeeResponse>> list(@RequestParam(required = false) String search, Pageable pageable) {
        return ApiResponse.success("Fees retrieved", feeService.listFees(search, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FEE_VIEW')")
    public ApiResponse<FeeResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("Fee retrieved", feeService.getFee(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<FeeResponse> create(@Valid @RequestBody FeeRequest request) {
        return ApiResponse.success("Fee created", feeService.createFee(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<FeeResponse> update(@PathVariable UUID id, @Valid @RequestBody FeeRequest request) {
        return ApiResponse.success("Fee updated", feeService.updateFee(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<FeeResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody FeeStatusRequest request) {
        return ApiResponse.success("Fee status updated", feeService.updateFeeStatus(id, request.status()));
    }
}
