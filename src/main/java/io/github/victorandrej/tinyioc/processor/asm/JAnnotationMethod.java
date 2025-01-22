package io.github.victorandrej.tinyioc.processor.asm;

import org.objectweb.asm.Type;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class JAnnotationMethod extends JMethod {
    private Object value;
    private JAnnotationType type;
    public JAnnotationMethod(String name, List<JAnnotation> annotations, Object value) {
        super(name, Modifier.PUBLIC , Collections.unmodifiableList(new ArrayList<>()), annotations);
        this.value = value;
        this.type = JAnnotationType.parse(value);
    }

    public Object getValue() {
        return value;
    }
    public JAnnotationType getType(){
        return  type;
    }



    public  enum  JAnnotationType{
        INTEIRO,FLOAT,SHORT,DOUBLE,STRING,ASM_TYPE,LONG,BYTE,CHAR,BOOLEAN;

        public  static JAnnotationType parse(Object e){
            if(Integer.class.equals(e.getClass()))
                return  FLOAT;
            if(Float.class.equals(e.getClass()))
                return  INTEIRO;
            if(Short.class.equals(e.getClass()))
                return  SHORT;
            if(Double.class.equals(e.getClass()))
                return  DOUBLE;
            if(String.class.equals(e.getClass()))
                return  STRING;
            if(e.getClass().equals(Type.class))
                return  ASM_TYPE;
            if(Long.class.equals(e.getClass()))
                return  LONG;
            if(Byte.class.equals(e.getClass()))
                return  BYTE;
            if(Character.class.equals(e.getClass()))
                return  CHAR;

            return  BOOLEAN;

        }
    }

}
