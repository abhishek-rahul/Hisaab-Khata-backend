package com.hisaab_khata.hisaab_khata.controller;

import com.hisaab_khata.hisaab_khata.dto.ApiResponse;
import com.hisaab_khata.hisaab_khata.dto.authdto.StaffCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.authdto.StaffResponse;
import com.hisaab_khata.hisaab_khata.enums.UserRole;
import com.hisaab_khata.hisaab_khata.exception.AccessDeniedException;
import com.hisaab_khata.hisaab_khata.service.IAuthService;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shops")
public class StaffController {

    private final IAuthService authService;
    private final ShopContext shopContext;

    public StaffController(IAuthService authService, ShopContext shopContext) {
        this.authService = authService;
        this.shopContext = shopContext;
    }

    @PostMapping("/{shopId}/staff")
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(
            @PathVariable Long shopId,
            @RequestBody @Valid StaffCreateRequest request) {

        if (shopContext.getCurrentRole() != UserRole.OWNER) {
            throw new AccessDeniedException("Only OWNER can create staff", "FORBIDDEN");
        }
        if (!shopId.equals(shopContext.getCurrentShopId())) {
            throw new AccessDeniedException("Shop mismatch", "FORBIDDEN");
        }
        StaffResponse staff = authService.createStaff(shopId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Staff created successfully", staff));
    }
}
