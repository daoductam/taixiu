package com.tamdao.taixiu_be.controller;

import com.tamdao.taixiu_be.dto.request.RedeemGiftCodeRequest;
import com.tamdao.taixiu_be.dto.response.ApiResponse;
import com.tamdao.taixiu_be.dto.response.UserResponse;
import com.tamdao.taixiu_be.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance(@AuthenticationPrincipal UserDetails userDetails) {
        BigDecimal balance = userService.getBalance(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(balance));
    }
    
    @PostMapping("/redeem-code")
    public ResponseEntity<ApiResponse<UserResponse>> redeemGiftCode(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RedeemGiftCodeRequest request) {
        UserResponse response = userService.redeemGiftCode(userDetails.getUsername(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success("Gift code redeemed successfully", response));
    }
    
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getLeaderboard() {
        List<UserResponse> leaderboard = userService.getLeaderboard();
        return ResponseEntity.ok(ApiResponse.success(leaderboard));
    }
}
