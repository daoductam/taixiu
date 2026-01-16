package com.tamdao.taixiu_be.controller;

import com.tamdao.taixiu_be.dto.response.ApiResponse;
import com.tamdao.taixiu_be.dto.response.UserResponse;
import com.tamdao.taixiu_be.entity.GiftCode;
import com.tamdao.taixiu_be.entity.User;
import com.tamdao.taixiu_be.service.AdminService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminService adminService;
    
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<User> users = adminService.getAllUsers(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(users));
    }
    
    @PostMapping("/users/{userId}/balance")
    public ResponseEntity<ApiResponse<UserResponse>> addBalance(
            @PathVariable Long userId,
            @RequestBody AddBalanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = adminService.addBalance(userId, request.getAmount(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Balance added successfully", response));
    }
    
    @PostMapping("/users/{userId}/toggle-status")
    public ResponseEntity<ApiResponse<String>> toggleUserStatus(@PathVariable Long userId) {
        adminService.toggleUserStatus(userId);
        return ResponseEntity.ok(ApiResponse.success("User status toggled"));
    }
    
    @GetMapping("/gift-codes")
    public ResponseEntity<ApiResponse<Page<GiftCode>>> getAllGiftCodes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<GiftCode> codes = adminService.getAllGiftCodes(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(codes));
    }
    
    @PostMapping("/gift-codes")
    public ResponseEntity<ApiResponse<GiftCode>> createGiftCode(
            @RequestBody CreateGiftCodeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        GiftCode code = adminService.createGiftCode(request.getAmount(), request.getExpiredAt(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Gift code created", code));
    }
    
    @DeleteMapping("/gift-codes/{id}")
    public ResponseEntity<ApiResponse<String>> deleteGiftCode(@PathVariable Long id) {
        adminService.deleteGiftCode(id);
        return ResponseEntity.ok(ApiResponse.success("Gift code deleted"));
    }
    
    @Data
    public static class AddBalanceRequest {
        private BigDecimal amount;
    }
    
    @Data
    public static class CreateGiftCodeRequest {
        private BigDecimal amount;
        private LocalDateTime expiredAt;
    }
}
