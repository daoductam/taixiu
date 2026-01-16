package com.tamdao.taixiu_be.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gift_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "is_used")
    @Builder.Default
    private Boolean isUsed = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_by")
    @JsonIgnoreProperties({"bets", "transactions", "hibernateLazyInitializer", "handler"})
    private User usedBy;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"bets", "transactions", "hibernateLazyInitializer", "handler"})
    private User createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return expiredAt != null && LocalDateTime.now().isAfter(expiredAt);
    }
    
    public boolean isValid() {
        return !isUsed && !isExpired();
    }
}
