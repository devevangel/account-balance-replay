package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintStream;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Main {
    public static void main(String[] args) throws Exception {
       // Always write UTF-8.
       PrintStream out = new PrintStream(System.out, false, StandardCharsets.UTF_8);
       PrintStream err = new PrintStream(System.err, false, StandardCharsets.UTF_8);

       if (args.length == 0) {
           err.print("Usage: supply the path to the event file\n");
           System.exit(1);
       }

       Path path = Paths.get(args[0]);
       ObjectMapper mapper = new ObjectMapper();
       Map<String, List<Event>> eventsByAccount = new HashMap<>();
       List<Account> flaggedAccounts = new ArrayList<>();
       List<Account> openedAccounts = new ArrayList<>();



       // Group events from file into individual list of events by accountId
       for (String line: Files.readAllLines(path, StandardCharsets.UTF_8)){
           if(line.isBlank()){
               continue;
           }
           Event event = mapper.readValue(line, Event.class);
           String id =  event.accountId();

           if(!eventsByAccount.containsKey(id)){
               eventsByAccount.put(id, new ArrayList<>());
           }
           eventsByAccount.get(id).add(event);
       }

       // Sort each account's events by seq, replay them, then keep opened
       // accounts for the result list and flagged accounts for REVIEW.
       for (List<Event> events : eventsByAccount.values()){
           events.sort((a, b) -> Integer.compare(a.seq(), b.seq()));

           Account account = new Account();
           for (Event event : events) {
               account.apply(event);
           }
           if (account.needsReview()) {
               flaggedAccounts.add(account);
           }
           if (!account.isOpened()) {
               continue;
           }
           openedAccounts.add(account);
       }

       // Flagged: sort by accountId. Opened: surname, then firstName, then accountId.
        flaggedAccounts.sort((a, b) -> a.getAccountId().compareTo(b.getAccountId()));
        openedAccounts.sort((a, b) -> {
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

        for (Account account : flaggedAccounts) {
            err.print(
                    "REVIEW "
                            + account.getAccountId()
                            + " "
                            + String.join("; ", account.getReviewReasons())
                            + "\n"
            );
        }

        for (Account account : openedAccounts) {
            out.print(
                    account.getSurname()
                            + " "
                            + account.getFirstName()
                            + " "
                            + account.getBalance().setScale(2, RoundingMode.UNNECESSARY)
                            + "\n"
            );
        }
        out.flush();
        err.flush();
    }
}