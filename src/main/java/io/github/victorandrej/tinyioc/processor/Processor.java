package io.github.victorandrej.tinyioc.processor;

import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.List;

public interface Processor {
    void process(Compiler compiler, Log log) throws Exception;
}
