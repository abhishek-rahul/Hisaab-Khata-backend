package com.hisaab_khata.hisaab_khata.controller;

import com.hisaab_khata.hisaab_khata.domain.User;
import com.hisaab_khata.hisaab_khata.dto.ApiResponse;
import com.hisaab_khata.hisaab_khata.dto.authdto.MeResponse;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.repository.UserRepository;
import com.hisaab_khata.hisaab_khata.util.ShopContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping
public class MeController {

    private final ShopContext shopContext;
    private final UserRepository userRepository;

    public MeController(ShopContext shopContext, UserRepository userRepository) {
        this.shopContext = shopContext;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me() {
        Optional<User> optUser = userRepository.findById(shopContext.getCurrentUserId());
        if (optUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found", "USER_NOT_FOUND");
        }
        User user = optUser.get();
        MeResponse response = MeResponse.builder()
                .id(user.getId())
                .shopId(user.getShopId())
                .name(user.getName())
                .mobile(user.getMobile())
                .role(user.getRole())
                .active(user.getActive())
                .build();
        return ResponseEntity.ok(ApiResponse.ok("Current user", response));
    }
}
