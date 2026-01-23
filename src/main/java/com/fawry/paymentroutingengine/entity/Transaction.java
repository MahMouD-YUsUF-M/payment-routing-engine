package com.fawry.paymentroutingengine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.fawry.paymentroutingengine.constant.Status;
import com.fawry.paymentroutingengine.constant.Urgency;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code", unique = true, nullable = false, length = 100)
    private String code;

    @Column(name = "biller_id", nullable = false)
    private Integer billerId;

    @Column(name = "gateway_id", nullable = false)
    private Integer gatewayId;


    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "commission", precision = 15, scale = 2, nullable = false)
    private BigDecimal commission;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency", length = 20, nullable = false)
    private Urgency urgency;  // INSTANT, CAN_WAIT

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status = Status.PENDING;

    @Column(name = "processing_time", length = 50)
    private String processingTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;



}



  /*

    Bouns Task
    @Column(name = "parent_transaction_id")
    private Long parentTransactionId;

    @Column(name = "is_split")
    private Boolean isSplit = false;

    @Column(name = "split_sequence")
    private Integer splitSequence;

    @Column(name = "total_splits")
    private Integer totalSplits;
    */

// setter, getter to string NO @Date
// equles , hash_code
// Data make all in to_string