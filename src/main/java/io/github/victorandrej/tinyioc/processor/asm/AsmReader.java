package io.github.victorandrej.tinyioc.processor.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AsmReader {
    public static JClass read(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ClassReader cr = new ClassReader(fis);
            JClassVisitor visitor = new JClassVisitor(Opcodes.ASM9);

            cr.accept(visitor, 0);

            return new JClass(visitor.name, visitor.modifier, visitor.methods, visitor.superClass, visitor.interfaces, visitor.annotations, visitor.constructors, visitor.filds, visitor.typedParameters);

        }


    }

    private static class JClassVisitor extends ClassVisitor {
        int modifier;
        String name;
        String superClass;
        List<String> interfaces = new ArrayList<>();
        List<JAnnotation> annotations = new ArrayList<>();
        List<JMethod> methods = new ArrayList<>();
        List<JField> filds = new ArrayList<>();
        List<JConstructor> constructors = new ArrayList<>();
        List<JParameter> typedParameters = new ArrayList<>();

        public JClassVisitor(int api) {
            super(api);

        }


        private List<JParameter> extractGenericTypes(String signature) {
            SignatureReader signatureReader = new SignatureReader(signature);
            List<JParameter> typedParameters = new ArrayList<>();
            String[] parameter = new String[1];
            signatureReader.accept(new SignatureVisitor(Opcodes.ASM9) {
                @Override
                public void visitFormalTypeParameter(String name) {
                    parameter[0] = name;
                }

                @Override
                public SignatureVisitor visitClassBound() {
                    return new SignatureVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitClassType(String name) {
                            typedParameters.add(new JParameter(parameter[0], name.replace('/', '.'), new ArrayList<>()));
                            parameter[0] = null;
                        }
                    };
                }

                @Override
                public SignatureVisitor visitInterfaceBound() {
                    return new SignatureVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitClassType(String name) {
                            typedParameters.add(new JParameter(parameter[0], name.replace('/', '.'), new ArrayList<>()));
                            parameter[0] = null;
                        }
                    };
                }
            });
            return typedParameters;
        }


        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.modifier = access;
            this.name = name.replace("/", ".");
            this.superClass = superName.replace("/", ".");
            if (signature != null)
                this.typedParameters = extractGenericTypes(signature);
            for (String s : interfaces)
                this.interfaces.add(s.replace("/", "."));

        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            var values = new ArrayList<JAnnotationMethod>();
            var an = new JAnnotation(descriptor.substring(1,descriptor.length()-1).replace("/", "."), values);
            this.annotations.add(an);
            return new AnnotationVisitor(Opcodes.ASM9) {
                @Override
                public void visit(String name, Object value) {
                    values.add(new JAnnotationMethod(name, new ArrayList<>(), value));
                }

                @Override
                public void visitEnum(String name, String descriptor, String value) {
                    values.add(new JAnnotationMethod(name, new ArrayList<>(), value));
                }
            };
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

            Type methodType = Type.getMethodType(descriptor);
            Type[] argumentTypes = methodType.getArgumentTypes();

            var parameters = new ArrayList<JParameter>();
            var annotations = new ArrayList<JAnnotation>();
            var method = name.equals("<init>") ? new JConstructor(access, parameters, annotations) : new JMethod(name, access, parameters, annotations);
            this.methods.add(method);
            return new MethodVisitor(Opcodes.ASM9) {
                int count = 0;

                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    var values = new ArrayList<JAnnotationMethod>();
                    var an = new JAnnotation(descriptor.substring(1,descriptor.length()-1).replace("/", "."), values);
                    annotations.add(an);
                    return new AnnotationVisitor(Opcodes.ASM9) {
                        @Override
                        public void visit(String name, Object value) {
                            values.add(new JAnnotationMethod(name, new ArrayList<>(), value));
                        }
                        @Override
                        public void visitEnum(String name, String descriptor, String value) {
                            values.add(new JAnnotationMethod(name, new ArrayList<>(), value));
                        }
                    };
                }


                @Override
                public void visitParameter(String name, int access) {
                    parameters.add(new JParameter(name, argumentTypes[count++].getClassName(), new ArrayList<>()));
                }


                @Override
                public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
                    if (parameters.isEmpty() || parameters.size() <= parameter)
                        return new AnnotationVisitor(Opcodes.ASM9) {
                        };
                    var parametro = parameters.get(parameter);
                    var values = new ArrayList<JAnnotationMethod>();
                    var an = new JAnnotation(descriptor.substring(1,descriptor.length()-1).replace("/", "."), values);
                    parametro.getAnnotations().add(an);
                    return new AnnotationVisitor(Opcodes.ASM9) {
                        @Override
                        public void visit(String name, Object value) {
                            values.add(new JAnnotationMethod(name, new ArrayList<>(), value));
                        }
                        @Override
                        public void visitEnum(String name, String descriptor, String value) {
                            values.add(new JAnnotationMethod(name, new ArrayList<>(), value));
                        }
                    };

                }
            };
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            var field = new JField(name, access, Type.getType(descriptor).getClassName());
            this.filds.add(field);
            return new FieldVisitor(Opcodes.ASM9) {
            };
        }
    }
}
