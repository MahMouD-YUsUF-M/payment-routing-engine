package com.fawry.paymentroutingengine.service;

import com.fawry.paymentroutingengine.dto.request.GatewayCreateRequest;
import com.fawry.paymentroutingengine.dto.request.GatewayUpdateRequest;
import com.fawry.paymentroutingengine.dto.response.GatewayResponse;
import com.fawry.paymentroutingengine.entity.Gateway;
import com.fawry.paymentroutingengine.entity.GatewayAvailability;
import com.fawry.paymentroutingengine.exception.DuplicateResourceException;
import com.fawry.paymentroutingengine.exception.ResourceNotFoundException;
import com.fawry.paymentroutingengine.repository.GatewayAvailabilityRepository;
import com.fawry.paymentroutingengine.repository.GateWayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GatewayService {

    private final GateWayRepository gatewayRepository;
    private final GatewayAvailabilityRepository availabilityRepository;

    @Transactional
    public GatewayResponse createGateway(GatewayCreateRequest request) {
        log.debug("Creating new gateway: {}", request.getName());

        // Auto-generate unique gateway code
        String generatedCode = generateUniqueGatewayCode(request.getName());
        log.debug("Generated gateway code: {}", generatedCode);

        Gateway gateway = new Gateway();
        gateway.setCode(generatedCode);
        gateway.setName(request.getName());
        gateway.setCommissionFixed(request.getCommissionFixed());
        gateway.setCommissionAmount(request.getCommissionPercentage());
        gateway.setMinTransaction(request.getMinTransaction());
        gateway.setMaxTransaction(request.getMaxTransaction() != null ? request.getMaxTransaction() : BigDecimal.ZERO);
        gateway.setDailyLimit(request.getDailyLimit());
        gateway.setProcessingTime(request.getProcessingTime());
        gateway.setIsActive(request.getIsActive());

        Gateway savedGateway = gatewayRepository.save(gateway);

        if (request.getAvailability() != null && !request.getAvailability().isEmpty()) {
            List<GatewayAvailability> availabilities = request.getAvailability().stream()
                    .map(availReq -> {
                        GatewayAvailability availability = new GatewayAvailability();
                        availability.setGatewayId(savedGateway.getId());
                        availability.setDayWeek(availReq.getDayOfWeek());
                        availability.setStartTime(availReq.getStartTime());
                        availability.setEndTime(availReq.getEndTime());
                        availability.setIs24_7(availReq.getIs24_7());
                        return availability;
                    })
                    .collect(Collectors.toList());

            availabilityRepository.saveAll(availabilities);
        }

        log.debug("Gateway created successfully: {}", savedGateway.getCode());
        return mapToResponse(savedGateway);
    }



    @Transactional(readOnly = true)
    public GatewayResponse getGatewayByCode(String code) {
        log.debug("Fetching gateway with code: {}", code);
        Gateway gateway = gatewayRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway not found with code: " + code));
        return mapToResponse(gateway);
    }
    @Transactional(readOnly = true)
    public List<GatewayResponse> getAllGateways() {
        log.debug("Fetching all gateways");
        return gatewayRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GatewayResponse updateGateway(String code, GatewayUpdateRequest request) {
        log.debug("Updating gateway with code: {}", code);

        Gateway gateway = gatewayRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway not found with code: " + code));

        // Gateway code CANNOT be updated (immutable)

        if (request.getName() != null) {
            gateway.setName(request.getName());
        }
        if (request.getCommissionFixed() != null) {
            gateway.setCommissionFixed(request.getCommissionFixed());
        }
        if (request.getCommissionPercentage() != null) {
            gateway.setCommissionAmount(request.getCommissionPercentage());
        }
        if (request.getMinTransaction() != null) {
            gateway.setMinTransaction(request.getMinTransaction());
        }
        if (request.getMaxTransaction() != null) {
            gateway.setMaxTransaction(request.getMaxTransaction());
        }
        if (request.getDailyLimit() != null) {
            gateway.setDailyLimit(request.getDailyLimit());
        }
        if (request.getProcessingTime() != null) {
            gateway.setProcessingTime(request.getProcessingTime());
        }
        if (request.getIsActive() != null) {
            gateway.setIsActive(request.getIsActive());
        }

        if (request.getAvailability() != null && !request.getAvailability().isEmpty()) {
            log.debug("Deleted  availabilities for gateway {}", gateway.getCode());

            int numberOfAvailabilities = availabilityRepository.deleteByGatewayId(gateway.getId());
            availabilityRepository.flush(); // ← CRITICAL: Force immediate delete
            log.debug("Deleted {}  availabilities for gateway {}",numberOfAvailabilities, gateway.getCode());

            List<GatewayAvailability> availabilities = request.getAvailability().stream()
                    .map(availReq -> {
                        GatewayAvailability availability = new GatewayAvailability();
                        availability.setGatewayId(gateway.getId());
                        availability.setDayWeek(availReq.getDayOfWeek());
                        availability.setStartTime(availReq.getStartTime());
                        availability.setEndTime(availReq.getEndTime());
                        availability.setIs24_7(availReq.getIs24_7());
                        return availability;
                    })
                    .collect(Collectors.toList());

            availabilityRepository.saveAll(availabilities);
        }

        Gateway updatedGateway = gatewayRepository.save(gateway);
        log.debug("Gateway updated successfully: {}", updatedGateway.getCode());

        return mapToResponse(updatedGateway);
    }

    public BigDecimal calculateCommission(String gatewayCode, BigDecimal amount) {
        Gateway gateway = gatewayRepository.findByCode(gatewayCode)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway not found with code: " + gatewayCode));

        return calculateCommissionForGateway(gateway, amount);
    }

    public BigDecimal calculateCommissionForGateway(Gateway gateway, BigDecimal amount) {
        BigDecimal fixedCommission = gateway.getCommissionFixed();
        BigDecimal percentageCommission = amount.multiply(gateway.getCommissionAmount());
        return fixedCommission.add(percentageCommission).setScale(2, RoundingMode.HALF_UP);
    }

    private GatewayResponse mapToResponse(Gateway gateway) {
        List<GatewayAvailability> availabilities = availabilityRepository.findByGatewayId(gateway.getId());

        return GatewayResponse.builder()
                .code(gateway.getCode())
                .name(gateway.getName())
                .commissionFixed(gateway.getCommissionFixed())
                .commissionPercentage(gateway.getCommissionAmount())
                .minTransaction(gateway.getMinTransaction())
                .maxTransaction(gateway.getMaxTransaction())
                .dailyLimit(gateway.getDailyLimit())
                .processingTime(gateway.getProcessingTime())
                .isActive(gateway.getIsActive())
                .availability(availabilities.stream()
                        .map(avail -> GatewayResponse.AvailabilitySchedule.builder()
                                .dayOfWeek(avail.getDayWeek())
                                .startTime(avail.getStartTime())
                                .endTime(avail.getEndTime())
                                .is24_7(avail.getIs24_7())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(gateway.getCreatedAt())
                .updatedAt(gateway.getUpdatedAt())
                .build();
    }

    /**
     * Generate unique gateway code from gateway name
     * Format: GW_UPPERCASENAME
     */
    private String generateUniqueGatewayCode(String gatewayName) {
        // Remove spaces and special characters, convert to uppercase
        String cleanName = gatewayName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

        // Take first 10 characters if name is too long
        if (cleanName.length() > 10) {
            cleanName = cleanName.substring(0, 10);
        }

        // Generate code with timestamp for uniqueness
        String baseCode = "GW_" + cleanName;
        String code = baseCode;
        int counter = 1;

        // Ensure uniqueness by adding counter if needed
        while (gatewayRepository.existsByCode(code)) {
            code = baseCode + "_" + counter;
            counter++;
        }

        return code;
    }
}