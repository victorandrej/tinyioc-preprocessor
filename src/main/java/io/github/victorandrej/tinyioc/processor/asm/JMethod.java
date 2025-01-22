package io.github.victorandrej.tinyioc.processor.asm;

import java.util.List;

public class JMethod {
    private int modifier;
    private String name;
    private List<JParameter> parameters;
    private List<JAnnotation> annotations;


    public JMethod(String name,int modifier, List<JParameter> parameters, List<JAnnotation> annotations) {
        this.name = name;
        this.modifier = modifier;
        this.parameters = parameters;
        this.annotations = annotations;
    }

    public String getName() {
        return name;
    }

    public int getModifier() {
        return modifier;
    }

    public List<JParameter> getParameters() {
        return parameters;
    }

    public List<JAnnotation> getAnnotations() {
        return annotations;
    }
}
