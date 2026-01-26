package com.fawry.paymentroutingengine.service;

import com.fawry.paymentroutingengine.dto.request.BillerCreateRequest;
import com.fawry.paymentroutingengine.dto.response.BillerResponse;
import com.fawry.paymentroutingengine.entity.Biller;
import com.fawry.paymentroutingengine.exception.DuplicateResourceException;
import com.fawry.paymentroutingengine.exception.ResourceNotFoundException;
import com.fawry.paymentroutingengine.repository.BillerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillerService {

    private final BillerRepository billerRepository;

    @Transactional
    public BillerResponse createBiller(BillerCreateRequest request) {

        String generatedCode = generateUniqueBillerCode(request.getName());

        if (billerRepository.existsByCode(request.getCodeBiller())) {
            throw new DuplicateResourceException("Biller with code '" + request.getCodeBiller() + "' already exists");
        }

        Biller biller = new Biller();
        biller.setCode(generatedCode);
        biller.setName(request.getName());
        biller.setEmail(request.getEmail());

        Biller savedBiller = billerRepository.save(biller);
        log.info("Biller created successfully: {}", savedBiller.getCode());

        return mapToResponse(savedBiller);
    }

    @Transactional(readOnly = true)
    public List<BillerResponse> getAllBillers() {
        log.info("Fetching all billers");
        return billerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BillerResponse getBillerByCode(String code) {
        log.info("Fetching biller with code: {}", code);
        Biller biller = billerRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Biller not found with code: " + code));
        return mapToResponse(biller);
    }

    private BillerResponse mapToResponse(Biller biller) {
        return BillerResponse.builder()
                .codeBiller(biller.getCode())
                .name(biller.getName())
                .email(biller.getEmail())
                .build();
    }
    private String generateUniqueBillerCode(String gatewayName) {
        String cleanName = gatewayName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

        if (cleanName.length() > 10) {
            cleanName = cleanName.substring(0, 10);
        }

        String baseCode = "BI_" + cleanName;
        String code = baseCode;
        int counter = 1;

        while (billerRepository.existsByCode(code)) {
            code = baseCode + "_" + counter;
            counter++;
        }

        return code;
    }
}