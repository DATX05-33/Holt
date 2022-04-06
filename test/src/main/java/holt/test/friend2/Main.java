package holt.test.friend2;

import holt.processor.annotation.DFD;

@DFD(name = "friend2", xml = "friend2.xml")
@DFD(name = "friend3", xml = "friend3.xml", privacyAware = true)
public class Main {
    public static void main(String[] args) {
        UserFormatter userFormatter = new UserFormatter();
        UserDatabase userDatabase = new UserDatabase();
        UserFetcher userFetcher = new UserFetcher();

        UserAdderEntity userAdderEntity = new UserAdderEntity(userFormatter, userDatabase);
        UserRetriever userRetriever = new UserRetriever(userDatabase, userFetcher);
    }
}
