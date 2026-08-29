package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Main {
    public static void main(String[] args) throws Exception {
       if (args.length == 0) {
           System.err.println("Usage: supply the path to the event file");
           System.exit(1);
       }

       Path path = Paths.get(args[0]);
       ObjectMapper mapper = new ObjectMapper();
       Map<String, List<Event>> eventsByAccount = new HashMap<>();
       List<Account> openedAccounts = new ArrayList<>();



       // Group events from file into individual list of events by accountId
       for (String line: Files.readAllLines(path)){
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
           if (!account.isOpened()) {
               continue;
           }
           openedAccounts.add(account);
       }


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

        for (Account account : openedAccounts) {
            System.out.println(
                    account.getSurname()
                            + " "
                            + account.getFirstName()
                            + " "
                            + account.getBalance().setScale(2, RoundingMode.UNNECESSARY)
            );
        }
    }
}