package holt.test.blast;

import holt.processor.annotation.DFD;

@DFD(name = "emailBlast", xml = "blast.xml", privacyAware = true)
public class Main {

    public static final String emailBlast = "emailBlast";

    public static void main(String[] args) {
        Company company = new Company();
    }
}
