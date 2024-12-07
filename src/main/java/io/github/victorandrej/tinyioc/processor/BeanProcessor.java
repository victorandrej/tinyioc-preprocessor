package io.github.victorandrej.tinyioc.processor;


import com.squareup.javapoet.JavaFile;
import io.github.victorandrej.tinyioc.processor.compiler.JavaCompile;
import io.github.victorandrej.tinyioc.processor.util.ClassUtil;
import io.github.victorandrej.tinyioc.processor.util.FileUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;

@Mojo(name = "processor-runner", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class BeanProcessor extends AbstractMojo {


    @Component
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/java", required = true)
    private File generetedSource;
    @Parameter(defaultValue = "${project.build.directory}/classes", required = true)
    private File classesDir;


    @Parameter()
    private List<String> processors;

    @Parameter(property = "skipProcessor", defaultValue = "false")
    private boolean skipProcessor;

    @Override
    public void execute() throws MojoExecutionException {
        if (skipProcessor)
            return;
        if (!generetedSource.exists()) {
            generetedSource.mkdirs();
        }
        File classesDir = new File(project.getBuild().getOutputDirectory());
        if (!classesDir.exists()) {
            throw new MojoExecutionException("O diretório de classes não foi encontrado: " + classesDir);
        }
        executeProcessors(classesDir);

    }

    private void executeClasses(Compiler compiler, ClassLoader classLoader) throws Exception {
        for (var clazzName : processors) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(clazzName, true, classLoader);
            } catch (ClassNotFoundException e) {
            }
            try {
                executeClass(clazz, compiler);
            } catch (Throwable e) {
                throw new Exception("Erro ao executar o processor " + clazzName + " ERRO: " + e);
            }


        }
    }

    private void executeClass(Class<?> clazz, Compiler compiler) throws Exception {
        if (!Processor.class.equals(clazz) && Processor.class.isAssignableFrom(clazz)) {
            try {

                var c = clazz.getConstructor();
                Processor p = (Processor) c.newInstance();
                p.process(compiler, getLog());
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
            List<File> classesFile = FileUtil.listFiles(classesDir, (f) -> f.getName().endsWith(".class"));

            CompilerImpl compiler = new CompilerImpl(ClassUtil.getAllClasses(classesFile, classesDir, classLoader, getLog()));
            executeClasses(compiler, classLoader);
            ;
            compileClasses(genereteJavaClasses(compiler));

        } catch (Throwable e) {
            throw new MojoExecutionException("Erro ao processar  o bean: ", e);
        }
    }

    private void compileClasses(List<Path> sourcePaths) throws IOException, MojoExecutionException {
        Set<File> dependencies = new HashSet<>();

        dependencies.addAll(getAllDependecies(project));
        dependencies.add(classesDir);

        new JavaCompile(sourcePaths, classesDir, dependencies, getLog()).compile();


    }

    private Set<File> getAllDependecies(MavenProject project) {
        Set<File> dependencies = new HashSet<>();
        var mavenDependencies = project.getArtifacts();
        if (Objects.nonNull(mavenDependencies))
            dependencies.addAll(mavenDependencies.stream().map(a -> a.getFile()).toList());
        return dependencies;
    }


    private List<Path> genereteJavaClasses(CompilerImpl compiler) throws IOException {
        List<Path> paths = new LinkedList<>();
        Path generetedSourcePath = generetedSource.toPath();
        for (var info : compiler.getClassToCompile()) {
            JavaFile.builder(info.packageName, info.typeSpec).build()
                    .writeTo(generetedSource);
            paths.add(generetedSourcePath.resolve(getClassPath(info.getPackageName(), info.typeSpec.name)));
        }
        return paths;
    }

    private Path getClassPath(String packageName, String className) {
        String path = packageName.replace('.', '/') + "/" + className + ".java";
        return Path.of(path);
    }

}