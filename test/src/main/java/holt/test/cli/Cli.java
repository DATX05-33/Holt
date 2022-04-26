package holt.test.cli;

import holt.processor.annotation.DFD;
import holt.test.cli.model.Policy.AccountManagement;
import holt.test.cli.model.Policy.Agreement;
import holt.test.cli.model.Policy.DeleteBefore;
import holt.test.cli.model.Policy.Marketing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@DFD(name = "cli", xml = "cli.xml")
public class Cli {

    private static final User user = new User();
    private static final Company company = new Company();
    private static final UserDB userDB = new UserDB();

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Cli started");

        String input = scanner.next();

        String[] command = input.split(" ");

        switch (command[0]) {
            case "user" -> userCases(command);
            case "company" -> companyCases(command);
            case "timeforward" -> Time.fastForward(command[1]);
            case "help" -> printHelp(true);
            //case "helpPA" -> printHelp(true);
            default -> throw new IllegalArgumentException("Illegal first argument " + command[0]);
        }
    }

    private static void printHelp(boolean b) {
        StringBuilder help = new StringBuilder();

        help.append("example: > user add me@email.com");
        help.append(b ? " \"login\" \"marketing\"\n" : "\n");

        help.append("example: > user add user@email.com");
        help.append(b ? " 10h \"login\"\n" : "\n");

        help.append("example: > user remove me@email.com\n");

        help.append("example: > company marketing \"Buy our product\"\n");
        help.append("example: > company resetpwd user@email.com\n");

        help.append("example: > timeforward 3h\n");

        System.out.println(help.toString());
    }

    private static void userCases(String[] args) {
        switch (args[1]) {
            case "add" -> addUser(args);
            case "remove" -> removeUser(args);
        }
    }

    private static void companyCases(String[] args) {
        switch (args[1]) {
            case "marketing" -> sendMarketing(args);
            case "resetpwd" -> resetpwd(args);
        }
    }

    private static void resetpwd(String[] args) {
        String email = args[2];
        // TODO: Via Company
    }

    private static void sendMarketing(String[] args) {
        String content = args[2];
        // TODO: Via Company
    }

    private static void removeUser(String[] args) {
        String email = args[2];

        // TODO: Go via User
        userDB.deleteUser(email);
    }

    private static void addUser(String[] args) {
        String email = args[2];
        String time = args[3];

        List<Agreement> agreements = new ArrayList<>();
        agreements.add(new DeleteBefore(Integer.getInteger(time)));

        List<String> argsList = new java.util.ArrayList<>(Arrays.stream(args).toList());
        argsList.subList(0, 4).clear();

        List<String> policies = new ArrayList<>(argsList);
        for (String pol : policies) {
            switch (pol) {
                case "\"login\"" -> agreements.add(new AccountManagement(true, true));
                case "\"marketing\"" -> agreements.add(new Marketing(true));
                default -> throw new IllegalArgumentException("Policy " + pol + " not allowed");
            }
        }

        // TODO: Go via User
        userDB.addUser(email);
    }
}
