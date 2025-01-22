package io.github.victorandrej.tinyioc.processor.asm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.List;

public class JClass {
    private static final int ANNOTATION = 0x00002000;
    int modifier;
    private String name;
    private int lastDotName;
    private List<JMethod> methods;
    private String superClass;
    private List<String> interfaces;
    private List<JAnnotation> annotations;
    private List<JConstructor> constructors;
    private List<JField> fields;
    private List<JParameter> typedParameters;

    public JClass(String name, int modifier, List<JMethod> methods, String superClass, List<String> interfaces, List<JAnnotation> annotations, List<JConstructor> constructors, List<JField> fields, List<JParameter> typedParameters) {
        this.name = name;
        this.modifier = modifier;
        this.lastDotName = name.lastIndexOf(".");
        this.methods = methods;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.annotations = annotations;
        this.constructors = constructors;
        this.fields = fields;
        this.typedParameters = typedParameters;
    }

    public List<JParameter> getTypedParameters(){
        return  this.typedParameters;

    }
    public <T extends Annotation> boolean hasAnnotation(Class< T > annotation) {
        return hasAnnotation(annotation.getName());
    }

    public boolean hasAnnotation(String annotation) {
        try {
            getAnnotation(annotation);
            return true;
        } catch (Exception e) {
            return false;
        }
    }




    public JAnnotation getAnnotation(String annotation) {
        return this.annotations.stream().filter(a -> a.getName().equals(annotation)).findFirst().orElseThrow();
    }

    public String getSimpleName() {
        return this.name.substring(lastDotName, name.length());
    }

    public String getPackageName() {
        return this.name.substring(0, lastDotName);
    }

    public String getName() {
        return name;
    }

    public  boolean isAnnotation(){
        return (getModifier() & ANNOTATION) != 0;
    }
    public int getModifier() {
        return modifier;
    }

    public List<JMethod> getMethods() {
        return methods;
    }

    public String getSuperClass() {
        return superClass;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public List<JAnnotation> getAnnotations() {
        return annotations;
    }

    public List<JConstructor> getConstructors() {
        return constructors;
    }

    public List<JField> getFields() {
        return fields;
    }
}
