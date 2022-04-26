package holt.test.blastPadfd;

import holt.processor.annotation.Activator;
import holt.processor.generation.blast_padfd.AbstractEmailBlaster;
import holt.test.blast.privacy.model.LimitBlastEmails;

@Activator
public class EmailBlaster extends AbstractEmailBlaster {
    @Override
    public void BLAST(LimitBlastEmails v) {

    }
}
