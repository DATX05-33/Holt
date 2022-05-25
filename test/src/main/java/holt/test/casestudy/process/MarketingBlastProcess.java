package holt.test.casestudy.process;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.annotation.Query;
import holt.processor.generation.casestudy.MarketingBlastReasonRequirements;
import holt.processor.generation.casestudy.MarketingBlastRequirements;
import holt.processor.generation.casestudy.UserDBToMarketingBlastProcessCreateEmailAndContentQuery;
import holt.test.casestudy.db.UserDB;
import holt.test.casestudy.model.EmailAndContent;
import holt.test.casestudy.model.EmailContent;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.UserPolicy;

import java.util.Collection;
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

    @Activator(instantiateWithReflection = true)
    public static class MarketingBlastProcessReason implements MarketingBlastReasonRequirements {
        @Override
        public Object M(Map<EmailContent, UserPolicy> input0, Map<User, UserPolicy> input1) {
            return null;
        }
    }


}
