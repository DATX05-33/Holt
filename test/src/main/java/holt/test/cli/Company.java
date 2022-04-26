package holt.test.cli;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Traverse;
import holt.test.cli.model.Email;
import holt.test.cli.model.MarketingContent;

@Traverse(
        name = "marketing",
        flowStartType = MarketingContent.class,
        order = {"emailContent", "getMarketingEmails", "blastMarketing"}
)
@Traverse(
        name = "resetPassword",
        flowStartType = Email.class,
        order = {"resetPwd", "getResetEmails", "sendOTP"}
)
@Activator
public class Company {
}
