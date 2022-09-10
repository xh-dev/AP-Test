package org.example.extraBuilder;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Attribute;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import me.xethh.utils.functionalPacks.Scope;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.example.extraBuilder.GenerationConfig.normalizingClassFromAnnotation;
import static org.example.extraBuilder.GenerationConfig.normalizingString;

@SupportedAnnotationTypes({
        "org.example.extraBuilder.ExtraBuilder",
        "org.example.extraBuilder.ExtraBuilderContainer",
        "org.example.extraBuilder.ContentClass"
})
//@SupportedAnnotationTypes({"org.example.extraBuilder.ExtraBuilder"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService({ExtraBuilder.class, ExtraBuilderContainer.class, ContentClass.class})
//@AutoService({ExtraBuilder.class})
public class ExtraBuilderProcessor extends AbstractProcessor {
    public static class XXX {
        private final Attribute.Constant code;
        private final Attribute.Class letter;
        private final Attribute.Class email;
        private final Attribute.Class sms;

        public XXX(Attribute.Constant code, Attribute.Class letter, Attribute.Class email, Attribute.Class sms) {
            this.code = code;
            this.letter = letter;
            this.email = email;
            this.sms = sms;
        }

        public Attribute.Constant getCode() {
            return code;
        }

        public Attribute.Class getLetter() {
            return letter;
        }

        public Attribute.Class getEmail() {
            return email;
        }

        public Attribute.Class getSms() {
            return sms;
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Start annotation processor");
        if (annotations.isEmpty()) return false;

        TypeElement typedElement = annotations.stream().collect(Collectors.toList()).get(0);

//        Element vl = typedElement.getEnclosedElements().get(0);
//        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(typedElement);

        Stream<Tuple3<Boolean, Element, XXX>> allExtraBuilder =
                new ArrayList<>(roundEnv.getElementsAnnotatedWith(ExtraBuilder.class)).stream()
                        .map(it -> (Element) it)
                        .map(it -> {
                                    List<? extends AnnotationValue> data = it.getAnnotationMirrors().get(0).getElementValues().values().stream().collect(Collectors.toList());
                                    XXX xx = new XXX(
                                            (Attribute.Constant) data.get(0),
                                            (Attribute.Class) data.get(1),
                                            (Attribute.Class) data.get(2),
                                            (Attribute.Class) data.get(3)
                                    );

                                    return Tuple.of(false, it, xx);
                                }
                        );

        Stream<Tuple3<Boolean, Element, XXX>> allExtraBuilderFromExtraBuilderContainer =
                roundEnv.getElementsAnnotatedWith(ExtraBuilderContainer.class).stream()
                        .map(it -> (Element) it)
                        .flatMap(it -> {

                                    List<Tuple3<Boolean, Element, XXX>> el = ((List<Attribute.Compound>) it
                                            .getAnnotationMirrors().get(0).getElementValues()
                                            .values().stream().findFirst().get().getValue())
                                            .stream().map(i -> {
                                                List<Attribute> data = i.getElementValues().values().stream().collect(Collectors.toList());
                                                XXX xx = new XXX(
                                                        (Attribute.Constant) data.get(0),
                                                        (Attribute.Class) data.get(1),
                                                        (Attribute.Class) data.get(2),
                                                        (Attribute.Class) data.get(3)
                                                );
                                                return Tuple.of(true, it, xx);
                                            }).collect(Collectors.toList());

                                    return el.stream();
                                }
                        );
        Stream.concat(
                allExtraBuilder,
                allExtraBuilderFromExtraBuilderContainer
        ).forEach(it -> {
            Element element = it._2;
            XXX annotation = it._3;

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing: " + annotation.getCode().getValue());

            GenerationConfig configuration = Scope.apply(new GenerationConfig(), config -> {
                String packageName = element.getEnclosingElement().toString();

                Tuple2<String, String> codePair = normalizingString.apply((String) annotation.getCode().value);
                TypeName letterVal = normalizingClassFromAnnotation.apply(annotation.getLetter().getValue());
                TypeName emailVal = normalizingClassFromAnnotation.apply(annotation.getEmail().getValue());
                TypeName smsVal = normalizingClassFromAnnotation.apply(annotation.getSms().getValue());
                String contentClassNameStr = "Content" + codePair._2;
                String containerClassNameStr = "GivenNotice" + codePair._2;

                config.setEnvironment(processingEnv);

                config.setContentClassStr(contentClassNameStr);
                config.setContainerClassStr(containerClassNameStr);
                config.setBasePackage(packageName);

                config.setLetter(letterVal);
                config.setEmail(emailVal);
                config.setSms(smsVal);
                config.setCodePair(codePair);
            });
            GenContentClass.genContentClass(configuration);
        });

        String contentClassName = "org.example.extraBuilder.ContentClass";
        if (annotations.stream().anyMatch(it -> it.getQualifiedName().contentEquals(contentClassName))) {
            roundEnv.getElementsAnnotatedWith(ContentClass.class)
                    .stream().flatMap(it -> {
                        return it.getAnnotationMirrors().stream().filter(i -> ((Attribute.Compound) i).type.toString().endsWith(contentClassName))
                                .map(i->Tuple.of(it, i))
                                ;
                    })
                    .forEach(it -> {

                        Attribute.Constant code = (Attribute.Constant) new ArrayList<>(((Attribute.Compound) it._2).getElementValues().values()).get(0);
                        GenerationConfig configuration = Scope.apply(new GenerationConfig(), config -> {
                            String packageName = it._1.getEnclosingElement().toString();

                            Tuple2<String, String> codePair = normalizingString.apply((String) code.value);
                            String contentClassNameStr = "Content" + codePair._2;
                            String containerClassNameStr = "GivenNotice" + codePair._2;

                            config.setEnvironment(processingEnv);

                            config.setContentClassStr(contentClassNameStr);
                            config.setContainerClassStr(containerClassNameStr);
                            config.setBasePackage(packageName);

                            config.setCodePair(codePair);
                        });
                        GenMainClass.genMainClass(configuration);
                    });
        }
        return false;
    }
}
