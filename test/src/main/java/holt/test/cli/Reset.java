package holt.test.cli;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.generation.cli.ResetRequirements;
import holt.processor.generation.cli.UserDBToResetGenerateOTPAndEmailQuery;
import holt.test.cli.model.Email;
import holt.test.cli.model.EmailAndContent;
import holt.test.cli.model.EmailContent;

@FlowThrough(
        traverse = "resetPassword",
        outputType = EmailAndContent.class,
        functionName = "generateOTPAndEmail",
        queries = {
                @Query(
                        db = UserDB.class,
                        type = holt.test.cli.model.User.class
                )
        }
)
@Activator
public class Reset implements ResetRequirements {
    @Override
    public UserDBToResetGenerateOTPAndEmailQuery queryUserDBGenerateOTPAndEmail(Email input0) {
        return db -> db.getUser(input0);
    }

    @Override
    public EmailAndContent generateOTPAndEmail(Email input0, holt.test.cli.model.User dbInput1) {
        if (dbInput1 == null) {
            throw new NullPointerException("User not found");
        }

        return new EmailAndContent(
                input0,
                new EmailContent(
                        "New pwd for " + dbInput1.email().email() + " is XXXYYYAAABBB. Very secure."
                )
        );

    }
}
