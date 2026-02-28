package com.hisaab_khata.hisaab_khata.controller;


import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.supplierdto.SupplierCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.supplierdto.SupplierResponse;
import com.hisaab_khata.hisaab_khata.service.ISupplierService;
import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/supplier")
@RequiredArgsConstructor
public class SupplierController {


    private final ISupplierService supplierService;

    @PostMapping
    public ResponseEntity<SuccessResponse<SupplierResponse>> create(
            @RequestBody SupplierCreateRequest request
    ) {
        SupplierResponse res = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                SuccessResponse.<SupplierResponse>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<SupplierResponse>>> index() {
        List<SupplierResponse> res = supplierService.getAllSuppliers();
        return ResponseEntity.ok(
                SuccessResponse.<List<SupplierResponse>>builder()
                        .success(true)
                        .data(res)
                        .build()
        );
    }
}

