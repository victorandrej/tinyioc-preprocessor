package io.github.victorandrej.tinyioc.processor;



import com.squareup.javapoet.JavaFile;
import io.github.victorandrej.tinyioc.processor.asm.AsmReader;
import io.github.victorandrej.tinyioc.processor.asm.JClass;
import io.github.victorandrej.tinyioc.processor.compiler.JavaCompile;

import io.github.victorandrej.tinyioc.processor.util.FileUtil;
import javassist.CtClass;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import javassist.ClassPool;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
    ClassPool classPool = ClassPool.getDefault();

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

                executeClass(clazz, compiler);

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

    private JClass createClass(File classFile) {
        try {

          return AsmReader.read(classFile);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void executeProcessors(File classesDir) throws MojoExecutionException {
        try {
            List<JClass> classes = new ArrayList();
            List<URL> processorsUrl = new ArrayList<>();
            FileUtil.listFiles(classesDir, (f) -> f.getName().endsWith(".class")).stream().forEach(file -> {

                var clazz = this.createClass(file);
                if (processors.contains(clazz.getName())) {
                    try {
                        processorsUrl.add(file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
                classes.add(clazz);


            });
            var classLoader = new UrlClassLoader(processorsUrl.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(classLoader);
            CompilerImpl compiler = new CompilerImpl(Collections.unmodifiableList(classes));
            executeClasses(compiler, classLoader);
            ;
            compileClasses(genereteJavaClasses(compiler));

        } catch (Throwable e) {
            e.printStackTrace();
            throw new MojoExecutionException("Erro ao processar  o bean: ", e);
        }
    }

    private void compileClasses(List<Path> sourcePaths) throws IOException, MojoExecutionException {
        Set<File> dependencies = new HashSet<>();

        dependencies.addAll(getAllDependecies(project));
        dependencies.add(classesDir);

        new JavaCompile(sourcePaths, classesDir,generetedSource, dependencies, getLog()).compile();


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