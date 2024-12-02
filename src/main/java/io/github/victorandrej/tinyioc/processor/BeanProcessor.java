package io.github.victorandrej.tinyioc.processor;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mojo(name = "processor-runner", defaultPhase = LifecyclePhase.COMPILE)
public class BeanProcessor extends AbstractMojo {


    @Component
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/java", required = true)
    private File generetedSourcer;

    @Parameter()
    private List<String> processors;

    @Parameter(property = "skipProcessor", defaultValue = "false")
    private boolean skipProcessor;

    @Override
    public void execute() throws MojoExecutionException {
        if (skipProcessor)
            return;
        if (!generetedSourcer.exists()) {
            generetedSourcer.mkdirs();
        }
        File classesDir = new File(project.getBuild().getOutputDirectory());
        if (!classesDir.exists()) {
            throw new MojoExecutionException("O diretório de classes não foi encontrado: " + classesDir);
        }
        executeProcessors(classesDir);

    }

    private void executeClass(Class<?> clazz, List<Class<?>> classes) throws Exception {
        if (!Processor.class.equals(clazz) && Processor.class.isAssignableFrom(clazz)) {
            try {

                var c = clazz.getConstructor();
                Processor p = (Processor) c.newInstance();
                p.process(generetedSourcer, classes, getLog());
            } catch (NoSuchMethodException ex) {
                getLog().info(clazz + " Sem construtor padrao, ignorando-o");
            }

        }
    }

    private void executeProcessors(File classesDir) throws MojoExecutionException {
        try (URLClassLoader classLoader = new URLClassLoader(
                new URL[]{classesDir.toURI().toURL()},
                Thread.currentThread().getContextClassLoader()
        )) {
            Thread.currentThread().setContextClassLoader(classLoader);
            List<Class<?>> allClasses = getAllClasses(classesDir, classLoader);
            var unmodifiabledAllClasses = Collections.unmodifiableList(allClasses);
            for (var clazzName : processors) {
                Class<?> clazz = null;
                try {
                    clazz = Class.forName(clazzName, true, classLoader);
                } catch (ClassNotFoundException e) {
                }
                try {
                    executeClass(clazz, unmodifiabledAllClasses);
                } catch (Throwable e) {
                    throw new Exception("Erro ao executar o processor " + clazzName+" ERRO: "+ e );
                }


            }
        } catch (Throwable e) {
            throw new MojoExecutionException("Erro ao processar  o bean: ", e);
        }
    }

    private List<Class<?>> getAllClasses(File classesDir, ClassLoader classLoader) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        for (File file : listClassFiles(classesDir)) {
            String className = toClassName(classesDir, file);
            try {

                classes.add(classLoader.loadClass(className));
            } catch (NoClassDefFoundError e) {
                getLog().warn("Não foi possível carregar a classe: " + className);
            }
        }
        return classes;
    }


    private List<File> listClassFiles(File dir) {
        List<File> classFiles = new ArrayList<>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                classFiles.addAll(listClassFiles(file));
            } else if (file.getName().endsWith(".class")) {
                classFiles.add(file);
            }
        }
        return classFiles;
    }

    private String toClassName(File rootDir, File classFile) {
        String relativePath = classFile.getAbsolutePath().substring(rootDir.getAbsolutePath().length() + 1);
        return relativePath.replace(File.separator, ".").replace(".class", "");
    }
}