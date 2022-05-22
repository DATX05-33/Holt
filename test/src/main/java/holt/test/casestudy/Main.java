package holt.test.casestudy;

import holt.processor.annotation.DFD;
import holt.test.casestudy.policy.AccountManagement;
import holt.test.casestudy.policy.Agreement;
import holt.test.casestudy.policy.DeleteBefore;
import holt.test.casestudy.policy.Marketing;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@DFD(name = "casestudy", xml = "casestudy.xml")
public class Main {

    private static User user;
    private static Company company;
    private static UserDB userDB = new UserDB();

    public static void main(String[] args) {
        user = new User();
        company = new Company();
        new Main();
    }

    public static UserDB getUserDB() {
        return userDB;
    }

    public Main() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Cli started");

        while (true) {
            System.out.print(">: ");
            String input = scanner.nextLine();

            String[] command = input.split(" ");

            try {
                switch (command[0]) {
                    case "user" -> userCases(command);
                    case "company" -> companyCases(command);
                    case "timeforward" -> Time.fastForward(command[1]);
                    case "help" -> printHelp(true);
                    case "exit" -> System.exit(0);
                    //case "helpPA" -> printHelp(true);
                    default -> System.out.println("Illegal first argument " + command[0]);
                }
            } catch (NullPointerException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void printHelp(boolean b) {
        StringBuilder help = new StringBuilder();

        help.append("example: > user add me@email.com");
        help.append(b ? " \"login\" \"marketing\"\n" : "\n");

        help.append("example: > user add user@email.com");
        help.append(b ? " 10h \"login\"\n" : "\n");

        help.append("example: > user remove me@email.com\n");

        help.append("example: > company marketing me@mail.com Buy Our Product\n");
        help.append("example: > company resetpwd user@email.com\n");

        help.append("example: > timeforward 3h\n");

        System.out.println(help.toString());
    }

    private void userCases(String[] args) {
        switch (args[1]) {
            case "add" -> addUser(args);
            case "remove" -> removeUser(args);
            default -> System.out.println("Illegal first argument " + args[1]);
        }
    }

    private void companyCases(String[] args) {
        switch (args[1]) {
            case "marketing" -> sendMarketing(args);
            case "resetpwd" -> resetpwd(args);
            default -> System.out.println("Illegal first argument " + args[1]);
        }
    }

    private void resetpwd(String[] args) {
        String email = args[2];
        company.resetPassword(email);
    }

    private void sendMarketing(String[] args) {
        String email = args[2];

        List<String> argsList = new java.util.ArrayList<>(Arrays.stream(args).toList());
        argsList.subList(0, 3).clear();

        StringBuilder content = new StringBuilder();
        for (String s : argsList) {
            content.append(s).append(" ");
        }
        company.sendMarketing(email, content.toString());
    }

    private void removeUser(String[] args) {
        String email = args[2];

        user.deleteUser(email);
    }

    private void addUser(String[] args) {
        String email = args[2];
        String time = args[3].substring(0, args[3].length()-1);


        List<Agreement> agreements = new ArrayList<>();
        agreements.add(new DeleteBefore(Integer.parseInt(time)));

        List<String> argsList = new java.util.ArrayList<>(Arrays.stream(args).toList());
        argsList.subList(0, 4).clear();

        List<String> policies = new ArrayList<>(argsList);
        for (String pol : policies) {
            switch (pol) {
                case "\"login\"" -> agreements.add(new AccountManagement(true, true));
                case "\"marketing\"" -> agreements.add(new Marketing(true));
                default -> System.out.println("Policy " + pol + " not available");
            }
        }

        user.addUser(email);
    }
}
