package com.tamdao.taixiu_be.controller;

import com.tamdao.taixiu_be.dto.request.PlaceBetRequest;
import com.tamdao.taixiu_be.dto.response.ApiResponse;
import com.tamdao.taixiu_be.dto.response.BetResponse;
import com.tamdao.taixiu_be.dto.response.GameSessionResponse;
import com.tamdao.taixiu_be.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {
    
    private final GameService gameService;
    
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<GameSessionResponse>> getCurrentGame() {
        GameSessionResponse response = gameService.getCurrentGame();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<GameSessionResponse.GameHistoryItem>>> getGameHistory() {
        List<GameSessionResponse.GameHistoryItem> history = gameService.getGameHistory();
        return ResponseEntity.ok(ApiResponse.success(history));
    }
    
    @PostMapping("/bet")
    public ResponseEntity<ApiResponse<BetResponse>> placeBet(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PlaceBetRequest request) {
        BetResponse response = gameService.placeBet(
                userDetails.getUsername(), 
                request.getBetType(), 
                request.getAmount()
        );
        return ResponseEntity.ok(ApiResponse.success("Bet placed successfully", response));
    }
}
