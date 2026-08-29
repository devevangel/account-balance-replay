package org.example;

import java.math.BigDecimal;

public record Event(
        String eventId,
        String accountId,
        int seq,
        String timestamp,
        String type,
        String firstName,
        String surname,
        String amount,
        BigDecimal rate,
        String targetEventId
) {}
