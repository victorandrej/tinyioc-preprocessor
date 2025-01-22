package io.github.victorandrej.tinyioc.processor;

import com.squareup.javapoet.TypeSpec;
import io.github.victorandrej.tinyioc.processor.asm.JClass;
import javassist.CtClass;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompilerImpl implements Compiler {
    private Set<CompileInfo> classToCompile = new HashSet<>();
    private List<JClass> sourceClasses;

    public CompilerImpl(List<JClass> sourceClasses) {

        this.sourceClasses = sourceClasses;
    }


    public Set<CompileInfo> getClassToCompile() {
        return classToCompile;
    }


    @Override
    public List<JClass> getSourceClasses() {
        return sourceClasses;
    }

    @Override
    public void compile(String packageName, TypeSpec typeSpec) {
        classToCompile.add(new CompileInfo(packageName, typeSpec));
    }

    class CompileInfo {
        String packageName;
        TypeSpec typeSpec;

        public CompileInfo(String packageName, TypeSpec typeSpec) {
            this.packageName = packageName;
            this.typeSpec = typeSpec;

        }

        public String getPackageName() {
            return packageName;
        }

        public TypeSpec getTypeSpec() {
            return typeSpec;
        }
    }

}
