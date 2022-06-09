package holt.test.casestudy.process;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.annotation.Query;
import holt.processor.generation.casestudy.MarketingBlastReasonRequirements;
import holt.processor.generation.casestudy.MarketingBlastRequirements;
import holt.processor.generation.casestudy.UserDBToMarketingBlastProcessCreateEmailAndContentQuery;
import holt.test.casestudy.db.UserDB;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.EmailAndContent;
import holt.test.casestudy.model.EmailContent;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.ContentAndUserPolicy;
import holt.test.casestudy.policy.MarketingType;
import holt.test.casestudy.policy.UserPolicy;

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


    // #5
    @Override
    public Collection<EmailAndContent> createEmailAndContent(EmailContent content, Collection<User> users) {
        return users.stream().map(user -> new EmailAndContent(user.email(), content)).toList();
    }

    // #3
    @Override
    public UserDBToMarketingBlastProcessCreateEmailAndContentQuery queryUserDBCreateEmailAndContent(EmailContent input0) {
        return userDB -> userDB.getUsers();
    }

    @FlowThrough(
            traverse = "M",
            output = @Output(type = ContentAndUserPolicy.class, collection = true),
            functionName = "reason"
    )
    @Activator(instantiateWithReflection = true)
    public static class MarketingBlastProcessReason implements MarketingBlastReasonRequirements {
        public Collection<ContentAndUserPolicy> reason(Map<EmailContent, MarketingType> input0, Map<User, UserPolicy> input1) {
            Collection<ContentAndUserPolicy> result = new ArrayList<>();
            for (UserPolicy x : input1.values()) {
                result.add(new ContentAndUserPolicy(input0.get(null), x));
            }
            return result;
        }

        @Override
        public Collection<ContentAndUserPolicy> reason(Map<EmailContent, MarketingType> input0,
                                                       Map<User, UserPolicy> input1,
                                                       Collection<EmailAndContent> input2) {
            Collection<ContentAndUserPolicy> result = new ArrayList<>();
            for (EmailAndContent eac: input2) {
                // This is kinda bad. We need the User here, not the email :(
                result.add(new ContentAndUserPolicy(input0.get(eac.content()), input1.get(eac.email())));
            }



            return result;
        }
    }
}
