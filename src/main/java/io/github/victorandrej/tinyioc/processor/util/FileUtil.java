package io.github.victorandrej.tinyioc.processor.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class FileUtil {
    public static List<File> listFiles(File dir, Function<File, Boolean> filter) {
        List<File> classFiles = new ArrayList<>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                classFiles.addAll(listFiles(file, filter));
            } else if (filter.apply(file)) {
                classFiles.add(file);
            }
        }
        return Collections.unmodifiableList( classFiles);
    }
}
