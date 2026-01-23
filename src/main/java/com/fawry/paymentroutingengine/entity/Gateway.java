package com.fawry.paymentroutingengine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gate_way")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gateway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "min_transaction", precision = 10, scale = 2, nullable = false)
    private BigDecimal minTransaction = BigDecimal.ZERO;

    @Column(name = "max_transaction", precision = 10, scale = 2, nullable = false)
    private BigDecimal maxTransaction = BigDecimal.ZERO;

    @Column(name = "commission_fixed", precision = 10, scale = 2)
    private BigDecimal commissionFixed = BigDecimal.ZERO;

    @Column(name = "commission_amount", precision = 10, scale = 10)
    private BigDecimal commissionAmount = BigDecimal.ZERO;

    @Column(name = "daily_limit", precision = 15, scale = 2, nullable = false)
    private BigDecimal dailyLimit = BigDecimal.ZERO;

    @Column(name = "Processing_time", precision = 20, scale = 2, nullable = false)
    private BigDecimal processingTime = BigDecimal.ZERO; // This will be in second and you must

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}


