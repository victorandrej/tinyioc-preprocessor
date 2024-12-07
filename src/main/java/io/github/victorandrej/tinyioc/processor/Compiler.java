package io.github.victorandrej.tinyioc.processor;

import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface Compiler {

    List<Class<?>> getSourceClasses();

    void compile(String packageName, TypeSpec typeSpec);
}
