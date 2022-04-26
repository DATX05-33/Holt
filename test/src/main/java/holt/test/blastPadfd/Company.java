package holt.test.blastPadfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Traverse;
import holt.processor.generation.blast_padfd.AbstractCompany;
import holt.test.blast.privacy.model.EmailContentPolicy;

import static holt.test.blastPadfd.Main.BLAST;

@Traverse(
        name = BLAST,
        flowStartType = EmailContentPolicy.class,
        order = {"CToRequest", "RToLimit1", "CToLimit1",
                "L1ToFetcher", "R1ToReason",
                "PDBToRequest", "R2ToLimit2",
                "DBToLimit2", "L2ToFetcher", "R2ToReason",
                "RToRequest", "RToLimit3", "FToLimit3",
                "RToBlaster", "LToBlaster"}
)
@Activator
public class Company extends AbstractCompany {
}
