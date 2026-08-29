package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Main {
    public static void main(String[] args) throws Exception {
       PrintWriter stderr = unixWriter(System.err);
       PrintWriter stdout = unixWriter(System.out);

       if (args.length == 0) {
           stderr.print("Usage: supply the path to the event file\n");
           stderr.flush();
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

       // Loop through each account's
       // Order each account's event by sequence
       //
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

        flaggedAccounts.sort((a, b) -> a.getAccountId().compareTo(b.getAccountId()));
        for (Account account : flaggedAccounts) {
            stderr.print(
                    "REVIEW "
                            + account.getAccountId()
                            + " "
                            + String.join("; ", account.getReviewReasons())
                            + "\n"
            );
        }
        stderr.flush();

        openedAccounts.sort((a, b) -> {
            int bySurname = name(a.getSurname()).compareTo(name(b.getSurname()));
            if (bySurname != 0) {
                return bySurname;
            }
            int byFirstName = name(a.getFirstName()).compareTo(name(b.getFirstName()));
            if (byFirstName != 0) {
                return byFirstName;
            }
            return a.getAccountId().compareTo(b.getAccountId());
        });

        for (Account account : openedAccounts) {
            stdout.print(
                    name(account.getSurname())
                            + " "
                            + name(account.getFirstName())
                            + " "
                            + account.getBalance().setScale(2, RoundingMode.UNNECESSARY)
                            + "\n"
            );
        }
        stdout.flush();
    }

    private static PrintWriter unixWriter(OutputStream stream) {
        return new PrintWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8), false);
    }

    private static String name(String value) {
        return value == null ? "" : value;
    }
}