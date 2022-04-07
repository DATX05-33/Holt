package holt.test.blast;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Traverse;
import holt.processor.generation.emailBlast.AbstractCompany;
import holt.test.blast.model.EmailContent;

import static holt.test.blast.Main.emailBlast;

@Traverse(
        flowStartType = EmailContent.class,
        name = emailBlast,
        order = {"wantBlast", "fetchEmails", "blastEmails"}
)
@Activator
public class Company extends AbstractCompany {
}
