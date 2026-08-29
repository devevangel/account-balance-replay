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

The sample file is not in this repo. `run.sh` builds a shaded jar, then runs `java -jar target/account-balance-replay.jar` with that path.

**Stdout** is result lines only. Usage errors go to **stderr**. If you omit the path, the program exits with code 1.

## Tests

```bash
mvn test
```

## Assumptions and decisions

- Replay is `seq` per `accountId`, not file order. The file interleaves accounts in time order; the brief defines history by `seq`, so grouping then sorting is the only way to apply events in the right sequence.
- Money is `BigDecimal` with `setScale(2, HALF_EVEN)`, not `double`. Binary floats cannot store tenths exactly, so penny rounding would be applied to noise. `HALF_EVEN` is the tie rule in the brief (`1.005 → 1.00`, `1.015 → 1.02`).
- A reverse subtracts the **posted** amount stored when that event was applied. The brief says reversing interest undoes the pennies that were actually posted, not `currentBalance × rate` again.
- Only accounts with `AccountOpened` are printed. The brief says that, and the sample file has two ids with money events but no open. Those have no name to print, so they are skipped.
- A reverse whose `targetEventId` is missing is skipped. One line in the sample file points at an event that does not exist. Failing the whole run on that would be stricter than the brief, which only describes reversing a posted event.
- Output is `surname firstName balance`, two decimal places, sorted by surname, then firstName, then accountId with `String.compareTo`, as specified.
