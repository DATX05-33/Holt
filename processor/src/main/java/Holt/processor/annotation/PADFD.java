package Holt.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//We just need to generate the wanted files, then we can discard this annotation.
@Retention(RetentionPolicy.SOURCE)
//We can only put this on class declarations
@Target(ElementType.TYPE)
@Inherited
public @interface PADFD {

    String name();
    String file();

}
