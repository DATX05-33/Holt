package holt.test.minitest;

import holt.processor.annotation.DFD;

@DFD(
        name = "minitest",
        xml = "minitest.xml",
        privacyAware = true
)
public class Main {

    public static void main(String[] args) {
        Stats stats = new Stats();
        System.out.println(stats.countAllWords());
    }

}
