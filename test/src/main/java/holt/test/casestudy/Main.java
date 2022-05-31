package holt.test.casestudy;

import holt.processor.annotation.DFD;
import holt.test.casestudy.db.UserDB;
import holt.test.casestudy.db.UserPolicyDB;
import holt.test.casestudy.entitiy.CompanyEntity;
import holt.test.casestudy.entitiy.MailSenderEntity;
import holt.test.casestudy.entitiy.UserEntity;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@DFD(name = "casestudy", xml = "casestudy.xml", privacyAware = true)
public class Main {

    private UserEntity userEntity;
    private CompanyEntity companyEntity;
    private UserDB userDB;
    private UserPolicyDB userPolicyDB;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        MailSenderEntity mailSenderEntity = new MailSenderEntity();

        this.userDB = new UserDB();
        this.userPolicyDB = new UserPolicyDB();
        this.userEntity = new UserEntity(this.userDB, this.userPolicyDB);
        this.companyEntity = new CompanyEntity(this.userPolicyDB, this.userDB, mailSenderEntity);

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
                    case "time" -> printTime();
                    case "timeforward" -> fastForwardTime(command);
                    case "listusers" -> listUser();
                    case "help" -> printHelp();
                    case "exit" -> System.exit(0);
                    default -> System.out.println("Illegal first argument " + command[0]);
                }
            } catch (NullPointerException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void fastForwardTime(String[] command) {
        if (command.length == 1) {
            System.out.println("Must specify how far to fast forward");
        } else {
            String t = command[1].substring(0, command[1].length() - 1);
            try {
                int h = Integer.parseInt(t);
                Time.fastForward(h);
                System.out.println("Time now: " + Time.getTime() + "h");
            } catch (NumberFormatException e) {
                System.out.println("Must be a whole number");
            }
        }
    }

    private void printTime() {
        System.out.println(Time.getTime() + "h");
    }

    private void listUser() {
        List<User> users = this.userDB.getUsers();
        if (users.size() == 0) {
            System.out.println("No users have been added");
        } else {
            users.forEach(System.out::println);
        }
    }

    private void printHelp() {
        StringBuilder help = new StringBuilder();

        help.append("example: > user add me@email.com 25h \"login\" \"marketing\"\n");

        help.append("example: > user add user@email.com 10h \"login\"\n");

        help.append("example: > user remove me@email.com\n");

        help.append("example: > company marketing me@mail.com Buy Our Product\n");
        help.append("example: > company resetpwd user@email.com\n");

        help.append("example: > timeforward 3h\n");

        System.out.println(help);
    }

    private void userCases(String[] args) {
        if (args.length == 1) {
            System.out.println("Missing args...");
        } else {
            switch (args[1]) {
                case "add" -> addUser(args);
                case "remove" -> removeUser(args);
                default -> System.out.println("Illegal first argument " + args[1]);
            }
        }
    }

    private void companyCases(String[] args) {
        switch (args[1]) {
            case "marketing" -> sendMarketing(args);
            case "resetpwd" -> resetPassword(args);
            default -> System.out.println("Illegal first argument " + args[1]);
        }
    }

    private void resetPassword(String[] args) {
        String email = args[2];
        companyEntity.resetPassword(email);
    }

    private void sendMarketing(String[] args) {
        companyEntity.sendMarketing(args[2].replace("\"", ""));
    }

    private void removeUser(String[] args) {
        String email = args[2];

        userEntity.deleteUser(email);
    }

    private void addUser(String[] args) {
        if (args.length < 2) {
            System.out.println("Missing email...");
        } else if (args.length < 3) {
            System.out.println("Missing deletion time");
        }

        String email = args[2];
        String time = args[3].substring(0, args[3].length()-1);

        List<AccessUserReason> agreements = new ArrayList<>();
        //agreements.add(new DeleteBefore(Integer.parseInt(time)));

        List<String> argsList = new ArrayList<>(Arrays.stream(args).toList());
        argsList.subList(0, 4).clear();

        List<String> policies = new ArrayList<>(argsList);
        for (String pol : policies) {
            switch (pol) {
                case "\"login\"" -> agreements.add(AccessUserReason.RESET_PASSWORD);//new AccountManagement(true, true));
                case "\"marketing\"" -> agreements.add(AccessUserReason.MARKETING);//new Marketing(true));
                default -> System.out.println("Policy " + pol + " not available");
            }
        }

        userEntity.addUser(email, agreements);
    }
}
