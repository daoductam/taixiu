package com.tamdao.taixiu_be.service;

import com.tamdao.taixiu_be.dto.response.UserResponse;
import com.tamdao.taixiu_be.entity.GiftCode;
import com.tamdao.taixiu_be.entity.Transaction;
import com.tamdao.taixiu_be.entity.User;
import com.tamdao.taixiu_be.repository.GiftCodeRepository;
import com.tamdao.taixiu_be.repository.TransactionRepository;
import com.tamdao.taixiu_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final GiftCodeRepository giftCodeRepository;
    private final TransactionRepository transactionRepository;
    
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }
    
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }
    
    public BigDecimal getBalance(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getBalance();
    }
    
    @Transactional
    public UserResponse redeemGiftCode(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        GiftCode giftCode = giftCodeRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Invalid gift code"));
        
        if (giftCode.getIsUsed()) {
            throw new RuntimeException("Gift code has already been used");
        }
        
        if (giftCode.isExpired()) {
            throw new RuntimeException("Gift code has expired");
        }
        
        // Apply gift code
        BigDecimal balanceBefore = user.getBalance();
        user.setBalance(user.getBalance().add(giftCode.getAmount()));
        
        // Mark code as used
        giftCode.setIsUsed(true);
        giftCode.setUsedBy(user);
        giftCode.setUsedAt(LocalDateTime.now());
        
        // Create transaction record
        Transaction transaction = Transaction.builder()
                .user(user)
                .type(Transaction.TransactionType.GIFT_CODE)
                .amount(giftCode.getAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(user.getBalance())
                .description("Redeemed gift code: " + code)
                .refId(giftCode.getId())
                .build();
        
        userRepository.save(user);
        giftCodeRepository.save(giftCode);
        transactionRepository.save(transaction);
        
        return mapToResponse(user);
    }
    
    public List<UserResponse> getLeaderboard() {
        return userRepository.findTop10ByOrderByBalanceDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .balance(user.getBalance())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .build();
    }
}
