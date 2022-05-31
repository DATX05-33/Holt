package holt.test.casestudy.policy;

import holt.test.casestudy.Time;

public class DeleteBefore implements Agreement {

    private final int hour;

    public DeleteBefore(int hour) {
        this.hour = hour;
    }

    public boolean shouldDelete() {
        return Time.getTime() > hour;
    }

}
