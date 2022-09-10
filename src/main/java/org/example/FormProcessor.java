package org.example;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Type;
import io.vavr.Tuple;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.example.extraBuilder.GenerationConfig.createGetter;
import static org.example.extraBuilder.GenerationConfig.createSetter;

@SupportedAnnotationTypes("org.example.FormBuilder")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(FormBuilder.class)
public class FormProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Executed processor -------------------------");
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            Set<? extends Element> lists = annotatedElements;
            for (Element e : lists) {
                String packageName = e.getEnclosingElement().toString();
                String className = e.getSimpleName().toString();
                String builderName = String.format("%sBuilder", className);
                String fullPathName = String.format("%s.%s", packageName, builderName);

                Map<String, ? extends AnnotationValue> map = e.getAnnotationMirrors()
                        .get(0)
                        .getElementValues()
                        .entrySet().stream()
                        .collect(Collectors.toMap(it -> it.getKey().toString(), Map.Entry::getValue));

                AnnotationValue field1 = map.get("field1()");
                AnnotationValue field2 = map.get("field2()");
                AnnotationValue field3 = map.get("field3()");

//                TypeName personTypeName = TypeName.get(e.asType());
                TypeName field1TypeName = TypeName.get((Type) field1.getValue());
                TypeName field2TypeName = TypeName.get((Type) field2.getValue());
                TypeName field3TypeName = TypeName.get((Type) field3.getValue());

                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Found package: " + packageName);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Found form: " + className);

                FieldSpec field1Spec = FieldSpec.builder(field1TypeName, "field1", Modifier.PRIVATE).build();
                FieldSpec field2Spec = FieldSpec.builder(field2TypeName, "field2", Modifier.PRIVATE).build();
                FieldSpec field3Spec = FieldSpec.builder(field3TypeName, "field3", Modifier.PRIVATE).build();
                MethodSpec method1Setter = createSetter(Tuple.of(field1TypeName, field1Spec)).build();
                MethodSpec method2Setter = createSetter(Tuple.of(field2TypeName, field2Spec)).build();
                MethodSpec method3Setter = createSetter(Tuple.of(field3TypeName, field3Spec)).build();
                MethodSpec method1Getter = createGetter(Tuple.of(field1TypeName, field1Spec)).build();
                MethodSpec method2Getter = createGetter(Tuple.of(field2TypeName, field2Spec)).build();
                MethodSpec method3Getter = createGetter(Tuple.of(field3TypeName, field3Spec)).build();

                MethodSpec main = MethodSpec.methodBuilder("main")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(String[].class, "args")
                        .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                        .build();

                TypeSpec helloWorld = TypeSpec.classBuilder(className + "Builder")
                        .addJavadoc("Generated by FormProcessor")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addField(field1Spec)
                        .addField(field2Spec)
                        .addField(field3Spec)
                        .addMethod(method1Setter)
                        .addMethod(method2Setter)
                        .addMethod(method3Setter)
                        .addMethod(method1Getter)
                        .addMethod(method2Getter)
                        .addMethod(method3Getter)
                        .addMethod(main)
                        .build();

                JavaFile javaFile = JavaFile.builder(packageName, helloWorld)
                        .build();

                try {
                    JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(fullPathName);
                    try (
                            Writer writer = builderFile.openWriter();
                    ) {
                        javaFile.writeTo(writer);
                        writer.flush();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return true;
    }

}
