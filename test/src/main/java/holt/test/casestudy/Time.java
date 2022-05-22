package holt.test.casestudy;

public class Time {

    private static int hours = 1;

    public static int getTime() {
        return hours;
    }

    public static void fastForward(int moreHours) {
        hours += moreHours;
    }
}
