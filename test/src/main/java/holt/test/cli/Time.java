package holt.test.cli;

import com.sun.jdi.IntegerType;

public class Time {

    private static int hours = 0;

    public static int getTime() {
        return hours;
    }

    public static void fastForward(int moreHours) {
        hours += moreHours;
    }

    public static void fastForward(String time) {
        assert time.charAt(time.length() - 1) == 'h' : "Input has to be in hours (last character \"h\")";

        time = time.substring(0, time.length() - 1);
        hours += Integer.parseInt(time);
    }

}
