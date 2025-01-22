package io.github.victorandrej.tinyioc.processor;


import javassist.ClassPool;
import javassist.CtClass;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class UrlClassLoader extends ClassLoader {
    ClassPool classPool = ClassPool.getDefault();

    public UrlClassLoader(URL[] urls,ClassLoader classLoader) throws URISyntaxException, IOException {
        super(classLoader);

        for (var url : urls) {
            File f = new File(url.toURI());
            var clazz = createClass(f);

            byte[] classBytes = Files.readAllBytes(f.toPath());

            defineClass(clazz.getName(), classBytes, 0, classBytes.length);

        }
    }

    private CtClass createClass(File classFile) {
        try {

            return  classPool.makeClass(new java.io.FileInputStream(classFile));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }



}


