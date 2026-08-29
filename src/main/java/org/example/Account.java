package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Account {
    private String accountId;
    private String firstName;
    private String surname;
    private boolean opened;
    private boolean needsReview;
    private final List<String> reviewReasons = new ArrayList<>();
    private BigDecimal balance = new BigDecimal("0.00");
    private final Map<String, BigDecimal> accountEvents = new HashMap<>();

    public void apply(Event event) {
        if (accountId == null) {
            accountId = event.accountId();
        }

        String type = event.type();

        switch (type) {
            case "AccountOpened" -> {
                accountId = event.accountId();
                firstName = event.firstName();
                surname = event.surname();
                balance = new BigDecimal("0.00");
                opened = true;
                if (firstName == null || firstName.isBlank()) {
                    flag("missing first name");
                }
                if (surname == null || surname.isBlank()) {
                    flag("missing surname");
                }
            }
            case "Credited" -> {
                if (!opened) {
                    flag("credited before account was opened");
                }
                BigDecimal amount = toPennies(new BigDecimal(event.amount()));
                balance = toPennies(balance.add(amount));
                accountEvents.put(event.eventId(), amount);
            }
            case "Debited" -> {
                if (!opened) {
                    flag("debited before account was opened");
                }
                BigDecimal amount = toPennies(new BigDecimal(event.amount()));
                balance = toPennies(balance.subtract(amount));
                accountEvents.put(event.eventId(), amount.negate());
            }
            case "InterestAccrued" -> {
                if (!opened) {
                    flag("interest before account was opened");
                }
                BigDecimal interest = toPennies(balance.multiply(event.rate()));
                balance = toPennies(balance.add(interest));
                accountEvents.put(event.eventId(), interest);
            }
            case "Reversed" -> {
                if (!opened) {
                    flag("reversed before account was opened");
                }
                BigDecimal posted = accountEvents.get(event.targetEventId());
                if (posted == null) {
                    flag("reverse target " + event.targetEventId() + " does not exist");
                } else {
                    balance = toPennies(balance.subtract(posted));
                }
            }
            default -> flag("unknown event type " + type);
        }
    }

    private void flag(String reason) {
        needsReview = true;
        reviewReasons.add(reason);
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

    public boolean needsReview() {
        return needsReview;
    }

    public List<String> getReviewReasons() {
        return reviewReasons;
    }
}
