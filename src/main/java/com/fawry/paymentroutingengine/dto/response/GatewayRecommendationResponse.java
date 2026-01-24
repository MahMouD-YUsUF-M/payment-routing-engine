package com.fawry.paymentroutingengine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import com.fawry.paymentroutingengine.constant.Urgency;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayRecommendationResponse {

    private RecommendedGateway recommendedGateway;
    private List<AlternativeGateway> alternatives;
    private String recommendationReason;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedGateway {
        private Long id;
        private String code;
        private String name;
        private BigDecimal estimatedCommission;
        private Urgency urgency;
        private BigDecimal remainingQuota;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlternativeGateway {
        private Long id;
        private String code;
        private String name;
        private Urgency urgency;
        private BigDecimal estimatedCommission;
        private String processingTime;
    }
}