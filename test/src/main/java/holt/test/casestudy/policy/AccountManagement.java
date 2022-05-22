package holt.test.casestudy.policy;

public class AccountManagement extends Agreement {

    boolean resetPassword;
    boolean login;

    public AccountManagement(boolean resetPassword, boolean login) {
        this.resetPassword = resetPassword;
        this.login = login;
    }
}