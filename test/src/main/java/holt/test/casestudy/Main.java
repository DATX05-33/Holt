package holt.test.casestudy;

import holt.processor.annotation.DFD;
import holt.test.casestudy.db.UserDB;
import holt.test.casestudy.entitiy.CompanyEntity;
import holt.test.casestudy.entitiy.MailSenderEntity;
import holt.test.casestudy.entitiy.UserEntity;
import holt.test.casestudy.model.User;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DFD(name = "casestudy", xml = "casestudy.xml")
public class Main {

    private final UserEntity userEntity;
    private final CompanyEntity companyEntity;
    private final UserDB userDB;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        MailSenderEntity mailSenderEntity = new MailSenderEntity();

        this.userDB = new UserDB();
        this.userEntity = new UserEntity(this.userDB);
        this.companyEntity = new CompanyEntity(this.userDB, mailSenderEntity);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Cli started");
        printHelp();

        while (true) {
            System.out.print(">: ");
            String input = scanner.nextLine();

            String[] command = input.split(" ");

            try {
                switch (command[0]) {
                    case "user" -> userCases(command);
                    case "company" -> companyCases(command);
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

        help.append("example: > user add me@example.org\n");

        help.append("example: > user add user@example.org\n");

        help.append("example: > user remove me@example.org\n");

        help.append("example: > listusers\n");

        help.append("example: > company marketing \"Buy Our Product\"\n");
        help.append("example: > company resetpwd user@example.org\n");

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
        String message = Stream.of(args).skip(2).collect(Collectors.joining(" ")).replace("\"", "");
        companyEntity.sendMarketing(message);
    }

    private void removeUser(String[] args) {
        String email = args[2];

        userEntity.deleteUser(email);
    }

    private void addUser(String[] args) {
        if (args.length < 2) {
            System.out.println("Missing email...");
        }

        String email = args[2];
        userEntity.addUser(email);
    }

}
