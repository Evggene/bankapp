package org.bea.service;

import lombok.RequiredArgsConstructor;
import org.bea.api.dto.ConversionRequest;
import org.bea.api.dto.ConversionResponse;
import org.bea.domain.ConversionOperation;
import org.bea.lib.ResilientCall;
import org.bea.repo.ConversionOperationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final RateClient rateClient;
    private final ConversionOperationRepository repo;

    @ResilientCall
    public ConversionResponse convert(ConversionRequest req) {
        Map<String, BigDecimal> rates = rateClient.getRatesMap();

        BigDecimal rateFrom = rates.getOrDefault(req.getFrom(), BigDecimal.ONE);
        BigDecimal rateTo   = rates.getOrDefault(req.getTo(),   BigDecimal.ONE);

        BigDecimal convRate = rateFrom.divide(rateTo, 6, RoundingMode.HALF_UP);
        BigDecimal result   = req.getAmount().multiply(convRate).setScale(2, RoundingMode.HALF_UP);

        var op = ConversionOperation.builder()
                .id(UUID.randomUUID())
                .ts(OffsetDateTime.now())
                .action(req.getAction() == null ? "BUY" : req.getAction().toUpperCase())
                .fromCurrency(req.getFrom())
                .toCurrency(req.getTo())
                .amount(req.getAmount())
                .rateFromRub(rateFrom.setScale(6, RoundingMode.HALF_UP))
                .rateToRub(rateTo.setScale(6, RoundingMode.HALF_UP))
                .conversionRate(convRate)
                .resultAmount(result)
                .build();

        repo.save(op);

        return ConversionResponse.builder()
                .id(op.getId())
                .action(op.getAction())
                .from(op.getFromCurrency())
                .to(op.getToCurrency())
                .amount(op.getAmount())
                .rateFromRub(op.getRateFromRub())
                .rateToRub(op.getRateToRub())
                .conversionRate(op.getConversionRate())
                .resultAmount(op.getResultAmount())
                .build();
    }
}
