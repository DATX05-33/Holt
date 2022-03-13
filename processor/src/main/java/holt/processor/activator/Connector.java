package holt.processor.activator;

import com.squareup.javapoet.ClassName;

public class Connector {

    private ClassName type;

    public void setType(ClassName type) {
        this.type = type;
    }

    public ClassName getType() {
        if (this.type == null) {
            return ClassName.get(Object.class);
        } else {
            return type;
        }
    }


}
