# AI notes

## What I asked it to do

- Read the assignment and `test_data.json`, then explain the problem in simple terms.
- Once I had an approach, break the work into small phases I could glance at and know what to build next.
- Break down my research on handling money in Java: why we keep amounts in precise decimal units with `BigDecimal` and `setScale`, and why `double` is the wrong type for cash.
- Review the tests I planned and flag any important cases I had missed.
- Help polish `README.md` and this file so they read cleanly.

I used it as a guide. I wrote the ledger code myself, in a style I can explain.

## What it got wrong or over-complicated

### Tests: it hid the data behind helpers

It wanted a helper so each test was one line. That meant I had to jump to another method to see what event I was applying.

What it suggested:

```java
account.apply(credit("e2", "1500.00"));
```

What I wrote instead, so the event is right there in the test:

```java
account.apply(new Event("e2", "a1", 2, "2026-01-01T00:00:00Z", "Credited", null, null, "1500.00", null, null));
```

The helper is shorter. I still rejected it. When I read a test I want to see the amount, the type, and the id without leaving the method.

### Event: it suggested a class with setters

It modelled `Event` as a normal class you can mutate after parse:

```java
Event event = new Event();
event.setAmount("1500.00");
event.setAmount("0.00"); // this should not be possible
```

These lines are historic facts. Once they are in the file they do not change. A `record` fits that:

```java
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
```

No setters. Jackson can still fill the fields from JSON.

## What I rejected, and why

- A mutable class for `Event`. A record is immutable and matches the data.
- Extra helper methods that only made the tests harder to read. I kept the tests explicit and short.
