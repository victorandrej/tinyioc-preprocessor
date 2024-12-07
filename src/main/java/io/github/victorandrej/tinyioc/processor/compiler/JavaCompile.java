package io.github.victorandrej.tinyioc.processor.compiler;

import io.github.victorandrej.tinyioc.processor.CompilerImpl;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class JavaCompile {
    private final List<Path> sourcePaths;
    private final File outDir;
    private final Set<File> dependencies;
    private final Log log;

    public JavaCompile(List<Path> sourcePaths, File outDir, Set<File> dependencies, Log log) {
        this.sourcePaths = sourcePaths;
        this.outDir = outDir;
        this.dependencies = dependencies;
        this.log = log;
    }

    public void compile() throws IOException, MojoExecutionException {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null)
            throw new RuntimeException("COMPILADOR NAO ESTA DISPONIVEL, ESTA USANDO UN JDK?");

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {

            Iterable<? extends JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjectsFromFiles(this.sourcePaths.stream().map(p->p.toFile()).toList());


            String classPath =  dependencies.stream().map(d->d.getAbsolutePath())
                    .reduce("",(s1,s2)-> "".equals(s1) ? s2 : (s1 +File.pathSeparator + s2) );

            List<String> params = Arrays.asList(

                    "-cp",classPath

            );

            JavaCompiler.CompilationTask task = compiler.getTask(
                    null, // Usar a saída padrão para erros
                    fileManager,
                    null, // Sem manipulação personalizada de erros
                    params, // Opções de compilação (saída em target/classes)
                    null, // Sem argumentos adicionais
                    compilationUnits // Unidades de compilação
            );

            boolean success = task.call();
            if (success) {
                log.info("Compilação bem-sucedida das classes dos processadores");
            } else {
               throw  new  MojoExecutionException( "Errro ao compilar Classes dos processadores");

            }
        }


    }
}
