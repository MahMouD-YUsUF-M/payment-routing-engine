package com.fawry.paymentroutingengine.service;

import com.fawry.paymentroutingengine.entity.Biller;
import com.fawry.paymentroutingengine.entity.DailyQuota;
import com.fawry.paymentroutingengine.entity.Gateway;
import com.fawry.paymentroutingengine.repository.BillerRepository;
import com.fawry.paymentroutingengine.repository.DailyQuotaRepository;
import com.fawry.paymentroutingengine.repository.GateWayRepository;
import com.fawry.paymentroutingengine.repository.GatewayAvailabilityRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuotaService {

    @Autowired
    private GateWayRepository gatewayRepository;

    @Autowired
    private BillerRepository billerRepository;

    @Autowired
    private DailyQuotaRepository quotaRepository;

        public BigDecimal getRemainingQuota(Long billerId, Long gatewayId) {
            LocalDate today =  LocalDate.now();

            return quotaRepository.findByBillerIdAndGatewayIdAndQuotaDate(billerId, gatewayId, today)
                    .map(quota -> quota.getDailyLimit().subtract(quota.getTotalAmount()))
                    .orElseGet(() -> {
                        // No quota record yet today - return full daily limit
                        Gateway gateway = gatewayRepository.findById(gatewayId).orElseThrow();
                        return gateway.getDailyLimit();
                    });
        }

    @Transactional
    public void updateQutoa( Long billerId, Long gatewayId, BigDecimal amount) {
        LocalDate today =  LocalDate.now();


        DailyQuota quota = quotaRepository
                .findByBillerIdAndGatewayIdAndQuotaDate(billerId, gatewayId, today)
                .orElse(null);

        if (quota == null) {
            // Create new quota record for today
            Gateway gateway = gatewayRepository.findById(gatewayId).orElseThrow();
            quota = new DailyQuota();
            quota.setBillerId(billerId);
            quota.setGatewayId(gatewayId);
            quota.setQuotaDate(today);
            quota.setTotalAmount(amount);
            quota.setTransactionCount(1);
            quota.setDailyLimit(gateway.getDailyLimit());
        } else {
            // Update existing quota
            quota.setTotalAmount(quota.getTotalAmount().add(amount));
            quota.setTransactionCount(quota.getTransactionCount() + 1);
        }

        quotaRepository.save(quota);

        log.info("Updated quota for biller {} on gateway {}: used {}/{}",
                billerId, gatewayId, quota.getTotalAmount(), quota.getDailyLimit());
    }
}
