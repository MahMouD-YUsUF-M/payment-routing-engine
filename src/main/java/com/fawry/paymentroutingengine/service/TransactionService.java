package com.fawry.paymentroutingengine.service;

import com.fawry.paymentroutingengine.constant.Status;
import com.fawry.paymentroutingengine.constant.Urgency;
import com.fawry.paymentroutingengine.dto.request.TransactionCreateRequest;
import com.fawry.paymentroutingengine.dto.response.TransactionResponse;
import com.fawry.paymentroutingengine.dto.response.TransactionSummaryResponse;
import com.fawry.paymentroutingengine.entity.Biller;
import com.fawry.paymentroutingengine.entity.DailyQuota;
import com.fawry.paymentroutingengine.entity.Gateway;
import com.fawry.paymentroutingengine.entity.Transaction;
import com.fawry.paymentroutingengine.exception.InsufficientQuotaException;
import com.fawry.paymentroutingengine.exception.InvalidTransactionException;
import com.fawry.paymentroutingengine.exception.ResourceNotFoundException;
import com.fawry.paymentroutingengine.repository.BillerRepository;
import com.fawry.paymentroutingengine.repository.DailyQuotaRepository;
import com.fawry.paymentroutingengine.repository.GateWayRepository;
import com.fawry.paymentroutingengine.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BillerRepository billerRepository;
    private final GateWayRepository gatewayRepository;
    private final DailyQuotaRepository quotaRepository;
    private final GatewayService gatewayService;

    @Transactional
    public TransactionResponse createTransaction(String billerCode, TransactionCreateRequest request) {
        log.info("Creating transaction for biller: {}, gateway: {}, amount: {}",
                billerCode, request.getGateWayCode(), request.getAmount());

        Biller biller = billerRepository.findByCode(billerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Biller not found: " + billerCode));

        Gateway gateway = gatewayRepository.findByCode(request.getGateWayCode())
                .orElseThrow(() -> new ResourceNotFoundException("Gateway not found: " + request.getGateWayCode()));

        if (!gateway.getIsActive()) {
            throw new InvalidTransactionException("Gateway is not active: " + gateway.getName());
        }

        if (request.getAmount().compareTo(gateway.getMinTransaction()) < 0) {
            throw new InvalidTransactionException("Amount below minimum transaction limit");
        }

        if (gateway.getMaxTransaction().compareTo(BigDecimal.ZERO) > 0 &&
                request.getAmount().compareTo(gateway.getMaxTransaction()) > 0) {
            throw new InvalidTransactionException("Amount exceeds maximum transaction limit");
        }

        BigDecimal remainingQuota = getRemainingQuota(biller.getId(), gateway.getId());
        if (remainingQuota.compareTo(request.getAmount()) < 0) {
            throw new InsufficientQuotaException("Insufficient daily quota. Remaining: " + remainingQuota);
        }

        BigDecimal commission = gatewayService.calculateCommissionForGateway(gateway, request.getAmount());

        Transaction transaction = new Transaction();
        transaction.setCode("TXN-" + UUID.randomUUID().toString());
        transaction.setBillerId(biller.getId().intValue());
        transaction.setGatewayId(gateway.getId().intValue());
        transaction.setAmount(request.getAmount());
        transaction.setCommission(commission);
        transaction.setUrgency(request.getUrgency());
        transaction.setStatus(Status.COMPLETED);
        transaction.setProcessingTime(gateway.getProcessingTime().toString() + " seconds");
        transaction.setCompletedAt(LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(transaction);

        updateQuota(biller.getId(), gateway.getId(), request.getAmount());

        log.info("Transaction created successfully: {}", savedTransaction.getCode());
        return mapToResponse(savedTransaction, biller, gateway);
    }

    @Transactional
    public void createTransaction(String billerCode, String gatewayCode, BigDecimal amount) {
        log.info("Creating transaction for biller: {}, gateway: {}, amount: {}",
                billerCode, gatewayCode, amount);

        Biller biller = billerRepository.findByCode(billerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Biller not found: " + billerCode));

        Gateway gateway = gatewayRepository.findByCode(gatewayCode)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway not found: " + gatewayCode));

        if (!gateway.getIsActive()) {
            throw new InvalidTransactionException("Gateway is not active: " + gateway.getName());
        }

        if (amount.compareTo(gateway.getMinTransaction()) < 0) {
            throw new InvalidTransactionException("Amount below minimum transaction limit");
        }

        if (gateway.getMaxTransaction().compareTo(BigDecimal.ZERO) > 0 &&
                amount.compareTo(gateway.getMaxTransaction()) > 0) {
            throw new InvalidTransactionException("Amount exceeds maximum transaction limit");
        }

        BigDecimal remainingQuota = getRemainingQuota(biller.getId(), gateway.getId());
        if (remainingQuota.compareTo(amount) < 0) {
            throw new InsufficientQuotaException("Insufficient daily quota. Remaining: " + remainingQuota);
        }

        BigDecimal commission = gatewayService.calculateCommissionForGateway(gateway, amount );

        Transaction transaction = new Transaction();
        transaction.setCode("TXN-" + UUID.randomUUID().toString());
        transaction.setBillerId(biller.getId().intValue());
        transaction.setGatewayId(gateway.getId().intValue());
        transaction.setAmount(amount);
        transaction.setCommission(commission);
        transaction.setUrgency(Urgency.INSTANT);
        transaction.setStatus(Status.COMPLETED);
        transaction.setProcessingTime(gateway.getProcessingTime().toString() + " seconds");
        transaction.setCompletedAt(LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(transaction);

        updateQuota(biller.getId(), gateway.getId(), amount);

        log.info("Transaction created successfully: {}", savedTransaction.getCode());

    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByBiller(String billerCode, LocalDate date) {
        log.info("Fetching transactions for biller: {}, date: {}", billerCode, date);

        Biller biller = billerRepository.findByCode(billerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Biller not found: " + billerCode));

        List<Transaction> transactions;

        if (date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            transactions = transactionRepository.findByBillerIdAndCreatedAtBetween(
                    biller.getId(), startOfDay, endOfDay);
        } else {
            transactions = transactionRepository.findByBillerId(biller.getId());
        }

        return transactions.stream()
                .map(txn -> {
                    Biller txnBiller = billerRepository.findById(txn.getBillerId()).orElse(biller);
                    Gateway txnGateway = gatewayRepository.findById(txn.getGatewayId().longValue()).orElse(null);
                    return mapToResponse(txn, txnBiller, txnGateway);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransactionSummaryResponse getTransactionSummary(String billerCode, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching transaction summary for biller: {}, period: {} to {}", billerCode, startDate, endDate);

        Biller biller = billerRepository.findByCode(billerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Biller not found: " + billerCode));

        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now();
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();

        LocalDateTime startDateTime = effectiveStartDate.atStartOfDay();
        LocalDateTime endDateTime = effectiveEndDate.plusDays(1).atStartOfDay();

        List<Transaction> transactions = transactionRepository.findByBillerIdAndCreatedAtBetween(
                biller.getId(), startDateTime, endDateTime);

        BigDecimal totalAmount = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCommission = transactions.stream()
                .map(Transaction::getCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Integer, List<Transaction>> byGateway = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getGatewayId));

        List<TransactionSummaryResponse.GatewaySummary> gatewaySummaries = byGateway.entrySet().stream()
                .map(entry -> {
                    Integer gatewayId = entry.getKey();
                    List<Transaction> gatewayTransactions = entry.getValue();

                    Gateway gateway = gatewayRepository.findById(gatewayId.longValue()).orElse(null);

                    BigDecimal gatewayTotal = gatewayTransactions.stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal gatewayCommission = gatewayTransactions.stream()
                            .map(Transaction::getCommission)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal avgCommission = gatewayTransactions.isEmpty() ? BigDecimal.ZERO :
                            gatewayCommission.divide(BigDecimal.valueOf(gatewayTransactions.size()), 2, RoundingMode.HALF_UP);

                    return TransactionSummaryResponse.GatewaySummary.builder()
                            .gatewayCode(gateway != null ? gateway.getCode() : "UNKNOWN")
                            .gatewayName(gateway != null ? gateway.getName() : "Unknown Gateway")
                            .transactionCount(gatewayTransactions.size())
                            .totalAmount(gatewayTotal)
                            .totalCommission(gatewayCommission)
                            .averageCommission(avgCommission)
                            .build();
                })
                .collect(Collectors.toList());

        Map<String, Integer> byStatus = transactions.stream()
                .collect(Collectors.groupingBy(
                        txn -> txn.getStatus().name(),
                        Collectors.summingInt(txn -> 1)
                ));

        return TransactionSummaryResponse.builder()
                .billerCode(biller.getCode())
                .period(TransactionSummaryResponse.Period.builder()
                        .startDate(effectiveStartDate)
                        .endDate(effectiveEndDate)
                        .build())
                .totalTransactions(transactions.size())
                .totalAmount(totalAmount)
                .totalCommission(totalCommission)
                .byGateway(gatewaySummaries)
                .byStatus(byStatus)
                .build();
    }

    private BigDecimal getRemainingQuota(Long billerId, Long gatewayId) {
        DailyQuota quota = quotaRepository.findByBillerIdAndGatewayIdAndQuotaDate(
                billerId, gatewayId, LocalDate.now()).orElse(null);

        Gateway gateway = gatewayRepository.findById(gatewayId)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway not found"));

        if (quota == null) {
            return gateway.getDailyLimit();
        }

        return gateway.getDailyLimit().subtract(quota.getTotalAmount());
    }

    @Transactional
    private void updateQuota(Long billerId, Long gatewayId, BigDecimal amount) {
        LocalDate today = LocalDate.now();
        DailyQuota quota = quotaRepository.findByBillerIdAndGatewayIdAndQuotaDate(
                billerId, gatewayId, today).orElse(null);

        Gateway gateway = gatewayRepository.findById(gatewayId)
                .orElseThrow(() -> new ResourceNotFoundException("Gateway not found"));

        if (quota == null) {
            quota = new DailyQuota();
            quota.setBillerId(billerId);
            quota.setGatewayId(gatewayId);
            quota.setQuotaDate(today);
            quota.setTotalAmount(amount);
            quota.setTransactionCount(1);
            quota.setDailyLimit(gateway.getDailyLimit());
        } else {
            quota.setTotalAmount(quota.getTotalAmount().add(amount));
            quota.setTransactionCount(quota.getTransactionCount() + 1);
        }

        quotaRepository.save(quota);
    }

    private TransactionResponse mapToResponse(Transaction transaction, Biller biller, Gateway gateway) {
        return TransactionResponse.builder()
                .code(transaction.getCode())
                .billerCode(biller != null ? biller.getCode() : "UNKNOWN")
                .gatewayCode(gateway != null ? gateway.getCode() : "UNKNOWN")
                .gatewayName(gateway != null ? gateway.getName() : "Unknown Gateway")
                .amount(transaction.getAmount())
                .commission(transaction.getCommission())
                .urgency(transaction.getUrgency())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
}