package com.fawry.paymentroutingengine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fawry.paymentroutingengine.constant.DayType;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "gateway_availability", uniqueConstraints = @UniqueConstraint(
        columnNames = {"gateway_id", "day_week"}
))

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatewayAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "gateway_id", nullable = false)
    private Long gatewayId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_week", nullable = false, length = 10)
    private DayType dayWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_24_7")
    private Boolean is24_7 = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    }

