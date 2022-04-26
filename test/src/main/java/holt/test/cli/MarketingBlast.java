package holt.test.cli;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.generation.cli.MarketingBlastRequirements;
import holt.processor.generation.cli.UserDBToMarketingBlastCreateEmailAndContentQuery;
import holt.test.cli.model.EmailAndContent;
import holt.test.cli.model.User;

@FlowThrough(
        traverse = "marketing",
        outputType = EmailAndContent.class,
        functionName = "createEmailAndContent",
        queries = {
                @Query(
                        db = UserDB.class,
                        type = holt.test.cli.model.User.class
                )
        }
)
@Activator
public class MarketingBlast implements MarketingBlastRequirements {

    @Override
    public UserDBToMarketingBlastCreateEmailAndContentQuery queryUserDBCreateEmailAndContent(EmailAndContent input0) {
        return db -> db.getUser(input0.email());
    }

    @Override
    public EmailAndContent createEmailAndContent(EmailAndContent input0, User dbInput1) {
        if (dbInput1 == null) {
            throw new NullPointerException("User not found");
        }

        return new EmailAndContent(dbInput1.email(), input0.content());
    }
}
