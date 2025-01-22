package io.github.victorandrej.tinyioc.processor;


import com.squareup.javapoet.TypeSpec;
import io.github.victorandrej.tinyioc.processor.asm.JClass;
import javassist.CtClass;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public interface Compiler {

    List<JClass> getSourceClasses();

    void compile(String packageName, TypeSpec typeSpec);
}
