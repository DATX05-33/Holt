package holt.test.friend2.friend3;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Traverse;
import holt.processor.generation.friend3.AbstractUser3;

@Traverse(
        name = "AF",
        order = {"AF1", "AF2"}
)
@Traverse(
        name = "GF",
        order = {"GF1", "GF2", "GF3"}
)
@Activator
public class User3 extends AbstractUser3 {

}
