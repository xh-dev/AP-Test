package org.example;

import com.google.auto.service.AutoService;
import dev.xethh.utils.WrappedResult.matching.ItemTransformer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ExecutableType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("org.example.FormBuilder")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(FormBuilder.class)
public class FormProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement typeElement : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(typeElement);
            Set<? extends Element> lists = annotatedElements;
            for (Element e : lists) {
                String packageName = e.getEnclosingElement().toString();
                String className = e.getSimpleName().toString();
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Found package: " + packageName);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Found form: " + className);

                ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
                List<? extends TypeParameterElement> typeParam = typeElement.getTypeParameters();
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Field1: "+formBuilder.field1().getName());
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Field2: "+formBuilder.field2().getName());
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Field3: "+formBuilder.field3().getName());
            }
        }
        return true;
    }
}
