package io.github.victorandrej.tinyioc.processor.asm;

import java.util.List;

public class JAnnotation {
    private String name;
    private List<JAnnotationMethod> values;


    public JAnnotation(String name, List<JAnnotationMethod> values) {
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public List<JAnnotationMethod> getValues() {
        return values;
    }
    public  JAnnotationMethod get(String name)
    {
        return  values.stream().filter(s->  s.getName().equals(name)).findFirst().orElseThrow();
    }


}
