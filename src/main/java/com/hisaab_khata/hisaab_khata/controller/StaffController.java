package com.hisaab_khata.hisaab_khata.controller;

import com.hisaab_khata.hisaab_khata.dto.ApiResponse;
import com.hisaab_khata.hisaab_khata.dto.authdto.StaffCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.authdto.StaffResponse;
import com.hisaab_khata.hisaab_khata.service.IAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shops")
public class StaffController {

    private final IAuthService authService;

    public StaffController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/{shopId}/staff")
    @PreAuthorize("hasRole('OWNER') and @shopSecurity.isCurrentShop(#shopId)")
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(
            @PathVariable Long shopId,
            @RequestBody @Valid StaffCreateRequest request) {
        StaffResponse staff = authService.createStaff(shopId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Staff created successfully", staff));
    }
}
