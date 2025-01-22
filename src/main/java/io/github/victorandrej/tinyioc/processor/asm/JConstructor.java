package io.github.victorandrej.tinyioc.processor.asm;

import java.util.List;

public class JConstructor extends JMethod {
    public JConstructor(int modifier,List<JParameter> parameters, List<JAnnotation> annotations) {
        super("<init>", modifier,parameters, annotations);
    }
}
