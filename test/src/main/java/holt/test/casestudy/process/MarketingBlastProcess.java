package holt.test.casestudy.process;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.annotation.Query;
import holt.processor.generation.casestudy.MarketingBlastRequirements;
import holt.processor.generation.casestudy.UserDBToMarketingBlastProcessCreateEmailAndContentQuery;
import holt.test.casestudy.db.UserDB;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.EmailAndContent;
import holt.test.casestudy.model.EmailContent;
import holt.test.casestudy.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@FlowThrough(
        traverse = "M",
        output = @Output(type = EmailAndContent.class, collection = true),
        functionName = "createEmailAndContent",
        queries = {
                @Query(
                        db = UserDB.class,
                        output = @Output(type = User.class, collection = true)
                )
        }
)
@Activator(instantiateWithReflection = true)
public class MarketingBlastProcess implements MarketingBlastRequirements {

    @Override
    public Collection<EmailAndContent> createEmailAndContent(EmailContent content, Collection<User> users) {
        return users.stream().map(user -> new EmailAndContent(user.email(), content)).toList();
    }

    @Override
    public UserDBToMarketingBlastProcessCreateEmailAndContentQuery queryUserDBCreateEmailAndContent(EmailContent input0) {
        return UserDB::getUsers;
    }

}
