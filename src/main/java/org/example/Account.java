package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class Account {
    private String accountId;
    private String firstName;
    private String surname;
    private boolean opened;
    private BigDecimal balance = new  BigDecimal("0.00");
    private final Map<String,BigDecimal> accountEvents = new HashMap<>();

    public void apply(Event event) {
        String type =  event.type();

        switch (type) {
            case "AccountOpened" -> {
                accountId = event.accountId();
                firstName = event.firstName();
                surname = event.surname();
                balance = new BigDecimal("0.00");
                opened = true;
            }
            case "Credited" -> {
                BigDecimal amount = toPennies(new BigDecimal(event.amount()));
                balance = toPennies(balance.add(amount));
                accountEvents.put(event.eventId(), amount);
            }
            case "Debited" -> {
                BigDecimal amount = toPennies(new BigDecimal(event.amount()));
                balance = toPennies(balance.subtract(amount));
                accountEvents.put(event.eventId(), amount.negate());
            }
            case "InterestAccrued" -> {
                BigDecimal interest = toPennies(balance.multiply(event.rate()));
                balance = toPennies(balance.add(interest));
                accountEvents.put(event.eventId(), interest);
            }
            case "Reversed" -> {
                BigDecimal posted = accountEvents.get(event.targetEventId());
                // One reverse in the file points at an event that does not exist
                if (posted != null) {
                    balance = toPennies(balance.subtract(posted));
                }
            }
        }

    }

    private static BigDecimal toPennies(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

    public String getAccountId() {
        return accountId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurname() {
        return surname;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public boolean isOpened() {
        return opened;
    }

}
