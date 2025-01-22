package io.github.victorandrej.tinyioc.processor.asm;

import java.util.List;

public class JParameter {
    private String name;
    private String type;
    private List<JAnnotation> annotations;

    public JParameter(String name, String type, List<JAnnotation> annotations) {
        this.name = name;
        this.type = type;

        this.annotations = annotations;
    }

    public String getType() {
        return this .type;
    }

    public String getName() {
        return name;
    }

    public List<JAnnotation> getAnnotations() {
        return annotations;
    }
}
