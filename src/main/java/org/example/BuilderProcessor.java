package org.example;

import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("org.example.BuilderProperty")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(BuilderProperty.class)
public class BuilderProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for(TypeElement typeElement: annotations){
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(typeElement);
            Map<Boolean, List<Element>> lists = annotatedElements.stream().collect(
                    Collectors.partitioningBy(e ->
                            ((ExecutableType) e.asType()).getParameterTypes().size() == 1
                                    && e.getSimpleName().toString().startsWith("set")
                    )
            );
            List<? extends Element> setters = lists.get(true);
            for(Element e : setters){
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Found setter: "+e.getSimpleName());


                try {
                    JavaFileObject builderFile = null;
                    builderFile = processingEnv.getFiler()
                            .createSourceFile("XXX");
                    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                        out.println("package org.example;\n" +
                                "\n" +
                                "public class XXX {\n" +
                                "    public static void main(String[] args) {\n" +
                                "    }\n" +
                                "}\n");
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            List<? extends Element> others = lists.get(false);
            for(Element e : others){
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Found other: "+e.getSimpleName());
            }



        }
        return false;
    }
}
