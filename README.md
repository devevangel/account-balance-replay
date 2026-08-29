# Account balance replay

Reads a banking event stream and prints the closing balance of every account that was opened.

## What you need

- JDK 17
- Maven
- Git

On Windows, use Git Bash or WSL so `./run.sh` works.

## Clone

```bash
git clone https://github.com/devevangel/account-balance-replay.git
cd account-balance-replay
```

## Run

From the repo root, pass the path to the JSON file that contains the event logs:

```bash
chmod +x run.sh
./run.sh /path/to/events.json
```

The main output is the balance lines only. Accounts that need a check print separately as `REVIEW <accountId> <reasons>`. If you do not provide the path, it prints a usage message and stops.

## Tests

```bash
mvn test
```

Thirteen tests covering: the five-event spec example, banker's rounding in both directions, interest on zero, negative balance, reverse of credit, reverse of interest, sort order by surname/firstName/accountId, same-name tiebreak, unopened account skipped, missing reversal target, missing name flagged, and two decimal places on output.

## Assumptions and decisions

- Replay is `seq` per `accountId`, not file order. The file interleaves accounts in time order; the brief defines history by `seq`, so grouping then sorting is the only way to apply events in the right sequence.
- Money is `BigDecimal` with `setScale(2, HALF_EVEN)`, not `double`. `double` cannot store values like `0.10` exactly, so pennies would be wrong. `HALF_EVEN` is the brief’s tie rule (`1.005 → 1.00`, `1.015 → 1.02`).
- A reverse subtracts the **posted** amount stored when that event was applied. The brief says reversing interest undoes the pennies that were actually posted, not `currentBalance × rate` again.
- Every opened account is still printed on stdout. Dirty rows (missing name, money before open, reverse of a missing event) are also flagged on stderr as `REVIEW <accountId> <reasons>` so a person can check them. We do not guess a “fixed” history.
- Output is `surname firstName balance`, two decimal places, sorted by surname, then firstName, then accountId with `String.compareTo`, as specified.
