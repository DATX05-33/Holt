package holt.test.friend2;

import holt.processor.annotation.DFD;

@DFD(name = "friend2", csv = "friend2.csv")
@DFD(name = "friend3", csv = "friend3.csv")
public class Main {
    public static void main(String[] args) {
        UserFormatter userFormatter = new UserFormatter();
        UserDatabase userDatabase = new UserDatabase();
        UserFetcher userFetcher = new UserFetcher();

        UserAdderEntity userAdderEntity = new UserAdderEntity(userFormatter, userDatabase);
        UserRetriever userRetriever = new UserRetriever(userDatabase, userFetcher);
    }
}
