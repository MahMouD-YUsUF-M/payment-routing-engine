package com.fawry.paymentroutingengine.service;

import com.fawry.paymentroutingengine.constant.Status;
import com.fawry.paymentroutingengine.dto.request.GatewayCreateRequest;
import com.fawry.paymentroutingengine.dto.request.GatewayUpdateRequest;
import com.fawry.paymentroutingengine.dto.response.GatewayResponse;
import com.fawry.paymentroutingengine.entity.Gateway;
import com.fawry.paymentroutingengine.entity.GatewayAvailability;
import com.fawry.paymentroutingengine.exception.DuplicateResourceException;
import com.fawry.paymentroutingengine.exception.ResourceNotFoundException;
import com.fawry.paymentroutingengine.repository.GatewayAvailabilityRepository;
import com.fawry.paymentroutingengine.repository.GatewayRepository;
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

    private final GatewayRepository gatewayRepository;
    private final GatewayAvailabilityRepository availabilityRepository;

    @Transactional
    public GatewayResponse createGateway(GatewayCreateRequest request) {
        log.info("Creating new gateway with code: {}", request.getCodeGateway());

        if (gatewayRepository.existsByCodeGateway(request.getCodeGateway())) {
            throw new DuplicateResourceException("Gateway with code '" + request.getCodeGateway() + "' already exists");
        }

        Gateway gateway = Gateway.builder()
                .codeGateway(request.getCodeGateway())
                .name(request.getName())
                .description(request.getDescription())
                .status(Status.ACTIVE)
                .commissionFixed(request.getCommissionFixed())
                .commissionPercentage(request.getCommissionPercentage())
                .minTransaction(request.getMinTransaction())
                .maxTransaction(request.getMaxTransaction())
                .dailyLimit(request.getDailyLimit())
                .processingTime(request.getProcessingTime())
                .build();

        Gateway savedGateway = gatewayRepository.save(gateway);

        if (request.getAvailability() != null && !request.getAvailability().isEmpty()) {
            List<GatewayAvailability> availabilities = request.getAvailability().stream()
                    .map(availReq -> GatewayAvailability.builder()
                            .gateway(savedGateway)
                            .dayOfWeek(availReq.getDayOfWeek())
                            .startTime(availReq.getStartTime())
                            .endTime(availReq.getEndTime())
                            .is24_7(availReq.getIs24_7())
                            .build())
                    .collect(Collectors.toList());

            availabilityRepository.saveAll(availabilities);
        }

        log.info("Gateway created successfully: {}", savedGateway.getCodeGateway());
        return mapToResponse(savedGateway);
    }

    @Transactional(readOnly = true)
    public List<GatewayResponse> getAllGateways() {
        log.info("Fetching all gateways");
        return gatewayRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GatewayResponse getGatewayByCode(String code) {
        log.info("Fetching gateway with code: {}", code);
        Gateway gateway = gatewayRepository.findByCodeGateway(code)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway not found with code: " + code));
        return mapToResponse(gateway);
    }

    @Transactional
    public GatewayResponse updateGateway(String code, GatewayUpdateRequest request) {
        log.info("Updating gateway with code: {}", code);

        Gateway gateway = gatewayRepository.findByCodeGateway(code)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway not found with code: " + code));

        if (request.getName() != null) {
            gateway.setName(request.getName());
        }
        if (request.getDescription() != null) {
            gateway.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            gateway.setStatus(request.getStatus());
        }
        if (request.getCommissionFixed() != null) {
            gateway.setCommissionFixed(request.getCommissionFixed());
        }
        if (request.getCommissionPercentage() != null) {
            gateway.setCommissionPercentage(request.getCommissionPercentage());
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

        if (request.getAvailability() != null && !request.getAvailability().isEmpty()) {
            availabilityRepository.deleteByGateway(gateway);

            List<GatewayAvailability> availabilities = request.getAvailability().stream()
                    .map(availReq -> GatewayAvailability.builder()
                            .gateway(gateway)
                            .dayOfWeek(availReq.getDayOfWeek())
                            .startTime(availReq.getStartTime())
                            .endTime(availReq.getEndTime())
                            .is24_7(availReq.getIs24_7())
                            .build())
                    .collect(Collectors.toList());

            availabilityRepository.saveAll(availabilities);
        }

        Gateway updatedGateway = gatewayRepository.save(gateway);
        log.info("Gateway updated successfully: {}", updatedGateway.getCodeGateway());

        return mapToResponse(updatedGateway);
    }

    public BigDecimal calculateCommission(String gatewayCode, BigDecimal amount) {
        Gateway gateway = gatewayRepository.findByCodeGateway(gatewayCode)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway not found with code: " + gatewayCode));

        return calculateCommissionForGateway(gateway, amount);
    }

    public BigDecimal calculateCommissionForGateway(Gateway gateway, BigDecimal amount) {
        BigDecimal fixedCommission = gateway.getCommissionFixed();
        BigDecimal percentageCommission = amount.multiply(gateway.getCommissionPercentage());
        return fixedCommission.add(percentageCommission).setScale(2, RoundingMode.HALF_UP);
    }

    private GatewayResponse mapToResponse(Gateway gateway) {
        List<GatewayAvailability> availabilities = availabilityRepository.findByGateway(gateway);

        return GatewayResponse.builder()
                .id(gateway.getIdGateway())
                .codeGateway(gateway.getCodeGateway())
                .name(gateway.getName())
                .description(gateway.getDescription())
                .status(gateway.getStatus())
                .commissionFixed(gateway.getCommissionFixed())
                .commissionPercentage(gateway.getCommissionPercentage())
                .minTransaction(gateway.getMinTransaction())
                .maxTransaction(gateway.getMaxTransaction())
                .dailyLimit(gateway.getDailyLimit())
                .processingTime(gateway.getProcessingTime())
                .availability(availabilities.stream()
                        .map(avail -> GatewayResponse.AvailabilityDTO.builder()
                                .dayOfWeek(avail.getDayOfWeek())
                                .startTime(avail.getStartTime())
                                .endTime(avail.getEndTime())
                                .is24_7(avail.getIs24_7())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(gateway.getCreatedAt())
                .updatedAt(gateway.getUpdatedAt())
                .build();
    }
}
