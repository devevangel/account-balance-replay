# Account balance replay

Reads a banking event stream (one JSON object per line) and prints each opened account’s closing balance.

## Requirements

- JDK 17
- Maven
- Git (to clone)

## Clone

```bash
git clone https://github.com/<your-username>/<your-repo>.git
cd <your-repo>
```

Replace the URL with the GitHub repo you created. If the repo is private, add the reviewer as a collaborator, then they clone the same way.

## Run

From the repo root, pass the path to the event file (the sample JSON is not in the repo):

```bash
chmod +x run.sh
./run.sh /path/to/events.json
```

`run.sh` runs `mvn -q -DskipTests package` (shaded jar with Jackson inside), then `java -jar target/account-balance-replay.jar`. Results go to **stdout**. Logs go to **stderr**.

## Tests

```bash
mvn test
```

## Assumptions

- An account’s history is `seq` order, not file order. Events are grouped by `accountId`, then sorted by `seq` before replay.
- Money is `BigDecimal`. Interest is `balance × rate`, rounded to the nearest penny with `RoundingMode.HALF_EVEN` (ties to the even penny).
- A reverse undoes the **posted** amount stored when that event was applied (the pennies for interest, not a new rate calculation).
- Only accounts with an `AccountOpened` event are printed. The sample file has two account ids with money events but no open; they are skipped.
- One reverse in the sample file points at an event id that does not exist. That reverse is skipped so the run does not fail.
- Output is `surname firstName balance`, exactly two decimal places, sorted by surname, then firstName, then accountId using `String.compareTo`.
