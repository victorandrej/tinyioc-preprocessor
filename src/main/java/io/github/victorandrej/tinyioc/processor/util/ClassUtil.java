package io.github.victorandrej.tinyioc.processor.util;

import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassUtil {
    public static List<Class<?>> getAllClasses(List<File> classesFile,File classesDir, ClassLoader classLoader, Log log) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        for (File file : classesFile) {
            String className = toClassName(classesDir, file);
            try {

                classes.add(classLoader.loadClass(className));
            } catch (NoClassDefFoundError e) {
                log.warn("Não foi possível carregar a classe: " + className + " motivo: " + e);
            }
        }
        return Collections.unmodifiableList(classes);
    }


    public static String toClassName(File rootDir, File classFile) {
        String relativePath = classFile.getAbsolutePath().substring(rootDir.getAbsolutePath().length() + 1);
        return relativePath.replace(File.separator, ".").replace(".class", "");
    }
}
