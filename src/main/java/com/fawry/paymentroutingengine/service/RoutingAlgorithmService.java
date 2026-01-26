package com.fawry.paymentroutingengine.service;

import com.fawry.paymentroutingengine.entity.Biller;
import com.fawry.paymentroutingengine.entity.Gateway;
import com.fawry.paymentroutingengine.entity.GatewayAvailability;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fawry.paymentroutingengine.constant.Urgency;
import com.fawry.paymentroutingengine.constant.DayType;
import com.fawry.paymentroutingengine.repository.BillerRepository;
import com.fawry.paymentroutingengine.repository.GatewayAvailabilityRepository;
import com.fawry.paymentroutingengine.repository.GateWayRepository;
import com.fawry.paymentroutingengine.dto.request.PaymentRecommendationRequest;
import com.fawry.paymentroutingengine.dto.response.GatewayRecommendationResponse;
import com.fawry.paymentroutingengine.exception.BillerNotFoundException;
import com.fawry.paymentroutingengine.exception.NoAvailableGatewayException;
import com.fawry.paymentroutingengine.service.TransactionService;
import com.fawry.paymentroutingengine.dto.request.TransactionCreateRequest;
import com.fawry.paymentroutingengine.service.TransactionService;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class RoutingAlgorithmService {

    @Autowired
    private GateWayRepository gatewayRepository;

    @Autowired
    private GatewayAvailabilityRepository availabilityRepository;

    @Autowired
    private BillerRepository billerRepository;

    @Autowired
    private QuotaService quotaService;

    @Autowired
    private TransactionService transactionService;

        public GatewayRecommendationResponse recommendGateway(PaymentRecommendationRequest request) {

            log.info("Starting gateway recommendation for biller: {}, amount: {}, urgency: {}",
                    request.getBillerCode() , request.getAmount(), request.getUrgency());

            Biller biller =  billerRepository.findByCode(request.getBillerCode())
                    .orElseThrow(() -> new BillerNotFoundException("Biller not found: " + request.getBillerCode()));

            List<Gateway> gateways = gatewayRepository.findByIsActiveTrue();
            log.debug("Found {} active gateways", gateways.size());

            gateways = gateways.stream()
                    .filter(g -> fitAmount(g , request.getAmount()))
                    .filter(this::isAvailableNow)
                    .filter(g -> hasQuotaRemaining(biller.getId(), g, request.getAmount()))
                    .collect(Collectors.toList());


            log.debug("After hard filters: {} gateways remaining", gateways.size());

            if (gateways.isEmpty()){
                    throw new NoAvailableGatewayException("No available gateway found for amount: " + request.getAmount());
            }

            if (request.getUrgency() == Urgency.INSTANT){
                    List<Gateway> instantGateways = gateways.stream()
                                                    .filter(g -> g.getProcessingTime().compareTo(BigDecimal.ZERO) == 0)
                                                    .collect(Collectors.toList());
                log.debug("After hard filters: {} INSTANT gateways remaining", gateways.size());

                if (!instantGateways.isEmpty()) {
                    gateways = instantGateways;
                    log.debug("Urgency=INSTANT: Filtered to {} instant gateways", gateways.size());
                } else {
                    log.warn("No instant gateways available, using all available gateways");
                }
            } else {
                log.debug("Urgency=CAN_WAIT: Using all {} available gateways", gateways.size());
            }

            List<ScoredGateway> scoredGateways = gateways.stream().
                    map(g -> scoreGateway(g, biller.getId(), request.getAmount()))
                    .sorted(
                            Comparator.comparing(ScoredGateway::getCommission).
                            thenComparing(ScoredGateway::getRemainingQuota , Comparator.reverseOrder())
                    ).collect(Collectors.toList());

            log.info("Sorted {} gateways. Best: {} with commission: {}",
                    scoredGateways.size(),
                    scoredGateways.get(0).getGateway().getCode(),
                    scoredGateways.get(0).getCommission());


            transactionService.createTransaction(request.getBillerCode(), scoredGateways.get(0).getGateway().getCode(), request.getAmount());

            return buildResponse(scoredGateways, request.getAmount());

        }


       private boolean fitAmount(Gateway gateway, BigDecimal amount) {

            if (amount.compareTo(gateway.getMinTransaction()) <= 0) {
                log.debug("Gateway {} rejected: amount {} < min {}",
                        gateway.getCode(), amount, gateway.getMinTransaction());
                return false;
            }

            if (amount.compareTo(gateway.getMaxTransaction()) >= 0) {
                log.debug("Gateway {} rejected: amount {} > max {}",
                        gateway.getCode(), amount, gateway.getMinTransaction());
                return false;
            }

            return true;
       }

       private boolean isAvailableNow(Gateway gateway) {
            LocalDateTime now = LocalDateTime.now();
            String Day = now.getDayOfWeek().toString().substring(0, 3);
            DayType currentDay = DayType.valueOf(Day);
            LocalTime currentTime = now.toLocalTime();

            List<GatewayAvailability> schedules = availabilityRepository
                    .findAvailabilityForDay(gateway.getId(), currentDay);

           if (schedules.isEmpty()) {
               log.debug("Gateway {} rejected: no availability schedule", gateway.getCode());
               return false;
           }

           for (GatewayAvailability schedule : schedules) {
               if (Boolean.TRUE.equals(schedule.getIs24_7())) {
                   log.debug("Gateway {} available: 24/7", gateway.getCode());
                   return true;
               }

               if (schedule.getStartTime() != null && schedule.getEndTime() != null) {
                   if (currentTime.isAfter(schedule.getStartTime()) &&
                           currentTime.isBefore(schedule.getEndTime())) {
                       log.debug("Gateway {} available: within time window", gateway.getCode());
                       return true;
                   }
               }
           }

           log.debug("Gateway {} rejected: outside availability window", gateway.getCode());
           return false ;

        }
       private boolean hasQuotaRemaining(Long  billerId, Gateway gateway, BigDecimal amount ) {
            BigDecimal remainingQuota = quotaService.getRemainingQuota(billerId, gateway.getId());

            boolean hasQuota = amount.compareTo(remainingQuota) <= 0;

           if (!hasQuota) {
               log.debug("Gateway {} rejected: insufficient quota. Required: {}, Remaining: {}",
                       gateway.getCode(), amount, remainingQuota);
           }

            return hasQuota;
       }
       private ScoredGateway scoreGateway(Gateway gateway, Long billerCode, BigDecimal amount) {

                 BigDecimal commission = calculateCommission(gateway, amount);
                 BigDecimal remainingQuota = BigDecimal.ZERO;


           return new ScoredGateway(gateway, commission, remainingQuota );

       }

       private BigDecimal calculateCommission(Gateway gateway, BigDecimal amount) {

                BigDecimal fixed = gateway.getCommissionFixed();
                BigDecimal percentage = gateway.getCommissionAmount();

                return fixed.add(percentage.multiply(amount));

       }


    private GatewayRecommendationResponse buildResponse(List<ScoredGateway> scoredGateways, BigDecimal amount) {

            ScoredGateway best = scoredGateways.get(0);

            GatewayRecommendationResponse.RecommendedGateway recommendedGateway
                    = GatewayRecommendationResponse.RecommendedGateway.builder()
                    .id(best.getGateway().getId())
                    .code(best.getGateway().getCode())
                    .name(best.getGateway().getName())
                    .estimatedCommission(best.getCommission())
                    .urgency(best.getGateway().getUrgencyFromProcessingTime())
                    .remainingQuota(best.getRemainingQuota())
                    .build();

        List<GatewayRecommendationResponse.AlternativeGateway> alternatives =
                scoredGateways.stream()
                        .skip(1)  // Skip the best one
                        .limit(2)  // Take max 2 alternatives
                        .map(sg -> GatewayRecommendationResponse.AlternativeGateway.builder()
                                .id(sg.getGateway().getId())
                                .code(sg.getGateway().getCode())
                                .name(sg.getGateway().getName())
                                .estimatedCommission(sg.getCommission())
                                .urgency(sg.getGateway().getUrgencyFromProcessingTime())
                                .build())
                        .collect(Collectors.toList());

        // Build reason
        String reason = String.format(
                "Lowest commission (%.2f EGP) among available gateways",
                best.getCommission()
        );

        return GatewayRecommendationResponse.builder()
                .recommendedGateway(recommendedGateway)
                .alternatives(alternatives)
                .recommendationReason(reason)
                .build();

    }
        /**
         * Internal class to hold gateway + score
         */
        @Data
        @AllArgsConstructor
        private static class ScoredGateway {
            private Gateway gateway;
            private BigDecimal commission;
            private BigDecimal remainingQuota;
        }

}
