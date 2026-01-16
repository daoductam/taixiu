package com.tamdao.taixiu_be.repository;

import com.tamdao.taixiu_be.entity.GiftCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GiftCodeRepository extends JpaRepository<GiftCode, Long> {
    
    Optional<GiftCode> findByCode(String code);
    
    boolean existsByCode(String code);
    
    Page<GiftCode> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<GiftCode> findByIsUsedOrderByCreatedAtDesc(Boolean isUsed, Pageable pageable);
}
