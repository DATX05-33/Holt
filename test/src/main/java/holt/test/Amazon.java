package holt.test;

import holt.processor.annotation.PADFD;

@PADFD(name = "amazon", file = "amazon-padfd.csv")
public class Amazon {

    public static void main(String[] args) {
        System.out.println("I am Amazon");
    }

}
