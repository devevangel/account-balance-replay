

import org.example.Account;
import org.example.Event;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AccountReplayTest {

    @Test
    @DisplayName("Open, add 1500, take 249.99, add interest, then undo the debit: the closing balance is 1505.47")
    void openCreditDebitInterestThenUndoTheDebit() {
        Account account = new Account();
        account.apply(new Event("e1", "a1", 1, "2026-01-01T00:00:00Z", "AccountOpened", "Ada", "Lovelace", null, null, null));
        account.apply(new Event("e2", "a1", 2, "2026-01-01T00:00:00Z", "Credited", null, null, "1500.00", null, null));
        account.apply(new Event("e3", "a1", 3, "2026-01-01T00:00:00Z", "Debited", null, null, "249.99", null, null));
        account.apply(new Event("e4", "a1", 4, "2026-01-01T00:00:00Z", "InterestAccrued", null, null, null, new BigDecimal("0.004375"), null));
        account.apply(new Event("e5", "a1", 5, "2026-01-01T00:00:00Z", "Reversed", null, null, null, null, "e3"));

        Assertions.assertEquals(new BigDecimal("1505.47"), account.getBalance());
        Assertions.assertFalse(account.needsReview());
        Assertions.assertTrue(account.getReviewReasons().isEmpty());

    }

    @Test
    @DisplayName("Interest of 1.005 pennies rounds down to 1.00 (tie goes to the even penny)")
    void interestTieRoundsToOne() {
        Account account = new Account();
        account.apply(new Event("e1", "a1", 1, "2026-01-01T00:00:00Z", "AccountOpened", "Ada", "Lovelace", null, null, null));
        account.apply(new Event("e2", "a1", 2, "2026-01-01T00:00:00Z", "Credited", null, null, "201.00", null, null));
        account.apply(new Event("e3", "a1", 3, "2026-01-01T00:00:00Z", "InterestAccrued", null, null, null, new BigDecimal("0.005"), null));

        Assertions.assertEquals(new BigDecimal("202.00"), account.getBalance());
    }


    @Test
    @DisplayName("Interest of 1.015 pennies rounds up to 1.02 (tie goes to the even penny)")
    void interestTieRoundsToOnePointZeroTwo() {
        Account account = new Account();
        account.apply(new Event("e1", "a1", 1, "2026-01-01T00:00:00Z", "AccountOpened", "Ada", "Lovelace", null, null, null));
        account.apply(new Event("e2", "a1", 2, "2026-01-01T00:00:00Z", "Credited", null, null, "203.00", null, null));
        account.apply(new Event("e3", "a1", 3, "2026-01-01T00:00:00Z", "InterestAccrued", null, null, null, new BigDecimal("0.005"), null));

        Assertions.assertEquals(new BigDecimal("204.02"), account.getBalance());
    }

    @Test
    @DisplayName("Interest on a zero balance adds nothing")
    void interestOnZero() {
        Account account = new Account();
        account.apply(new Event("e1", "a1", 1, "2026-01-01T00:00:00Z", "AccountOpened", "Ada", "Lovelace", null, null, null));
        account.apply(new Event("e2", "a1", 2, "2026-01-01T00:00:00Z", "InterestAccrued", null, null, null, new BigDecimal("0.005"), null));

        Assertions.assertEquals(new BigDecimal("0.00"), account.getBalance());
    }

    @Test
    @DisplayName("A debit with no credit can leave a negative balance")
    void debitCanGoNegative() {
        Account account = new Account();
        account.apply(new Event("e1", "a1", 1, "2026-01-01T00:00:00Z", "AccountOpened", "Kenji", "Nakamura", null, null, null));
        account.apply(new Event("e2", "a1", 2, "2026-01-01T00:00:00Z", "Debited", null, null, "500.00", null, null));

        Assertions.assertEquals(new BigDecimal("-500.00"), account.getBalance());
    }

    @Test
    @DisplayName("Undoing a credit puts the balance back to zero")
    void undoCredit() {
        Account account = new Account();
        account.apply(new Event("e1", "a1", 1, "2026-01-01T00:00:00Z", "AccountOpened", "Ada", "Lovelace", null, null, null));
        account.apply(new Event("e2", "a1", 2, "2026-01-01T00:00:00Z", "Credited", null, null, "1000.00", null, null));
        account.apply(new Event("e3", "a1", 3, "2026-01-01T00:00:00Z", "Reversed", null, null, null, null, "e2"));

        Assertions.assertEquals(new BigDecimal("0.00"), account.getBalance());
    }

    @Test
    @DisplayName("Undoing interest removes only the pennies that were added, not a new calculation")
    void undoInterest() {
        Account account = new Account();
        account.apply(new Event("e1", "a1", 1, "2026-01-01T00:00:00Z", "AccountOpened", "Ada", "Lovelace", null, null, null));
        account.apply(new Event("e2", "a1", 2, "2026-01-01T00:00:00Z", "Credited", null, null, "1000.00", null, null));
        account.apply(new Event("e3", "a1", 3, "2026-01-01T00:00:00Z", "InterestAccrued", null, null, null, new BigDecimal("0.005"), null));
        account.apply(new Event("e4", "a1", 4, "2026-01-01T00:00:00Z", "Reversed", null, null, null, null, "e3"));

        Assertions.assertEquals(new BigDecimal("1000.00"), account.getBalance());
    }

    @Test
    @DisplayName("Accounts are listed by surname, then first name, then account id")
    void sortByNameThenId() {
        Account zebraAna = new Account();
        zebraAna.apply(new Event("e1", "acc-002", 1, "2026-01-01T00:00:00Z", "AccountOpened", "Ana", "Zebra", null, null, null));
        Account ahmedBob = new Account();
        ahmedBob.apply(new Event("e1", "acc-001", 1, "2026-01-01T00:00:00Z", "AccountOpened", "Bob", "Ahmed", null, null, null));
        Account ahmedAna = new Account();
        ahmedAna.apply(new Event("e1", "acc-003", 1, "2026-01-01T00:00:00Z", "AccountOpened", "Ana", "Ahmed", null, null, null));
        List<Account> accounts = new ArrayList<>();
        accounts.add(zebraAna);
        accounts.add(ahmedBob);
        accounts.add(ahmedAna);
        accounts.sort((a, b) -> {
            int bySurname = a.getSurname().compareTo(b.getSurname());
            if (bySurname != 0) {
                return bySurname;
            }
            int byFirstName = a.getFirstName().compareTo(b.getFirstName());
            if (byFirstName != 0) {
                return byFirstName;
            }
            return a.getAccountId().compareTo(b.getAccountId());
        });
        Assertions.assertEquals("acc-003", accounts.get(0).getAccountId());
        Assertions.assertEquals("acc-001", accounts.get(1).getAccountId());
        Assertions.assertEquals("acc-002", accounts.get(2).getAccountId());
    }

    @Test
    @DisplayName("Two people with the same name: the smaller account id comes first")
    void sameNameSmallerIdFirst() {
        Account laterId = new Account();
        laterId.apply(new Event("e1", "acc-100", 1, "2026-01-01T00:00:00Z", "AccountOpened", "Joan", "Ahmed", null, null, null));
        Account earlierId = new Account();
        earlierId.apply(new Event("e1", "acc-050", 1, "2026-01-01T00:00:00Z", "AccountOpened", "Joan", "Ahmed", null, null, null));
        List<Account> accounts = new ArrayList<>();
        accounts.add(laterId);
        accounts.add(earlierId);
        accounts.sort((a, b) -> a.getAccountId().compareTo(b.getAccountId()));

        Assertions.assertEquals("acc-050", accounts.get(0).getAccountId());
        Assertions.assertEquals("acc-100", accounts.get(1).getAccountId());
    }

    @Test
    @DisplayName("An account that was never opened is not treated as a customer")
    void neverOpened() {
        Account account = new Account();
        account.apply(new Event("e1", "acc-0232", 1, "2026-01-01T00:00:00Z", "Credited", null, null, "100.00", null, null));
        account.apply(new Event("e2", "acc-0232", 2, "2026-01-01T00:00:00Z", "Credited", null, null, "50.00", null, null));

        Assertions.assertFalse(account.isOpened());
        Assertions.assertTrue(account.needsReview());
        Assertions.assertEquals(List.of("credited before account was opened"), account.getReviewReasons());
    }

    @Test
    @DisplayName("Undoing an event that does not exist does not change the balance")
    void undoMissingEvent() {
        Account account = new Account();
        account.apply(new Event("e1", "a1", 1, "2026-01-01T00:00:00Z", "AccountOpened", "Ada", "Lovelace", null, null, null));
        account.apply(new Event("e2", "a1", 2, "2026-01-01T00:00:00Z", "Credited", null, null, "100.00", null, null));
        account.apply(new Event("e3", "a1", 3, "2026-01-01T00:00:00Z", "Reversed", null, null, null, null, "e-missing"));

        Assertions.assertTrue(account.isOpened());
        Assertions.assertEquals(new BigDecimal("100.00"), account.getBalance());
        Assertions.assertTrue(account.needsReview());
        Assertions.assertTrue(account.getReviewReasons().contains("reverse target e-missing does not exist"));
    }

    @Test
    @DisplayName("A missing first name or surname is flagged for review")
    void missingNameIsFlagged() {
        Account account = new Account();
        account.apply(new Event("e1", "a1", 1, "2026-01-01T00:00:00Z", "AccountOpened", null, "", null, null, null));

        Assertions.assertTrue(account.isOpened());
        Assertions.assertTrue(account.needsReview());
        Assertions.assertTrue(account.getReviewReasons().contains("missing first name"));
        Assertions.assertTrue(account.getReviewReasons().contains("missing surname"));
    }

    @Test
    @DisplayName("The balance is always written with two digits after the dot")
    void twoDecimalPlaces() {
        Account account = new Account();
        account.apply(new Event("e1", "a1", 1, "2026-01-01T00:00:00Z", "AccountOpened", "Ada", "Lovelace", null, null, null));
        account.apply(new Event("e2", "a1", 2, "2026-01-01T00:00:00Z", "Credited", null, null, "10.00", null, null));
        Assertions.assertEquals(2, account.getBalance().scale());
        Assertions.assertEquals("10.00", account.getBalance().toPlainString());
    }


}
