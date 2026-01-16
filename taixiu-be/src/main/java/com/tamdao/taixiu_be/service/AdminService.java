package com.tamdao.taixiu_be.service;

import com.tamdao.taixiu_be.dto.response.UserResponse;
import com.tamdao.taixiu_be.entity.GiftCode;
import com.tamdao.taixiu_be.entity.Transaction;
import com.tamdao.taixiu_be.entity.User;
import com.tamdao.taixiu_be.repository.GiftCodeRepository;
import com.tamdao.taixiu_be.repository.TransactionRepository;
import com.tamdao.taixiu_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final UserRepository userRepository;
    private final GiftCodeRepository giftCodeRepository;
    private final TransactionRepository transactionRepository;
    
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    @Transactional
    public UserResponse addBalance(Long userId, BigDecimal amount, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        BigDecimal balanceBefore = user.getBalance();
        user.setBalance(user.getBalance().add(amount));
        
        // Create transaction
        Transaction transaction = Transaction.builder()
                .user(user)
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(user.getBalance())
                .description("Admin " + adminUsername + " added balance")
                .build();
        
        userRepository.save(user);
        transactionRepository.save(transaction);
        
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .balance(user.getBalance())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .build();
    }
    
    @Transactional
    public GiftCode createGiftCode(BigDecimal amount, LocalDateTime expiredAt, String adminUsername) {
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        String code = "GIFT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        GiftCode giftCode = GiftCode.builder()
                .code(code)
                .amount(amount)
                .expiredAt(expiredAt)
                .createdBy(admin)
                .build();
        
        return giftCodeRepository.save(giftCode);
    }
    
    public Page<GiftCode> getAllGiftCodes(Pageable pageable) {
        return giftCodeRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    @Transactional
    public void deleteGiftCode(Long id) {
        GiftCode giftCode = giftCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gift code not found"));
        
        if (giftCode.getIsUsed()) {
            throw new RuntimeException("Cannot delete used gift code");
        }
        
        giftCodeRepository.delete(giftCode);
    }
    
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }
}
