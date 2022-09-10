package org.example.extraBuilder;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Type;
import dev.xethh.utils.WrappedResult.matching.ItemTransformer;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenerationConfig {
    public static MethodSpec.Builder createGetter(Tuple2<TypeName, FieldSpec> tuple) {
        TypeName typeName = tuple._1;
        FieldSpec fieldSpec = tuple._2;
        return MethodSpec.methodBuilder(String.format("%s%s", "get", capitalFirstChar.apply(fieldSpec.name)))
                .returns(typeName)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return $L", fieldSpec.name)
                ;
    }

    public static MethodSpec.Builder createSetter(Tuple2<TypeName, FieldSpec> tuple) {
        TypeName typeName = tuple._1;
        FieldSpec fieldSpec = tuple._2;
        return MethodSpec.methodBuilder(String.format("%s%s", "set", capitalFirstChar.apply(fieldSpec.name)))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(typeName, fieldSpec.name).build())
                .addStatement("this.$L = $L", fieldSpec.name, fieldSpec.name)
                ;
    }
    static Function<String, String> capitalFirstChar =
            (String s) -> ItemTransformer.transfer(String.class, String.class)
                    .isNull().thenValue("")
                    .inCase(it -> it.length() <= 1).then(it -> it.toUpperCase(Locale.ROOT))
                    .defaultValueTransform(it -> it.substring(0, 1).toUpperCase(Locale.ROOT) + it.substring(1).toLowerCase())
                    .matches(s);
    public static Function<String, Tuple2<String, String>> normalizingString =
            (String s) -> {
                String replace = s
                        .replaceAll("([A-Z]+)", "-$0")
                        .replaceAll("^-", "")
                        .replaceAll("-$", "")
                        .replaceAll("[-]{2,}", "-")
                        .replace(" ", "-")
                        .replace("_", "-");
                return Tuple.of(s,
                        Arrays.stream(
                                        replace
                                                .split("-")
                                )
                                .map(it -> capitalFirstChar.apply(it))
                                .collect(Collectors.joining())
                );
            };
    public static Function<AnnotationValue, TypeName> normalizingClass =
            (AnnotationValue field) -> TypeName.get((Type) field.getValue());
    public static Function<Type, TypeName> normalizingClassFromAnnotation = TypeName::get;

    public static BiFunction<String, String, String> fullPathName = (packageStr, className) -> String.format("%s.%s", packageStr, className);
    private ProcessingEnvironment environment;
    private String basePackage;

    private Tuple2<String, String> codePair;

    private String contentClassStr;

    public TypeName contentClassName() {
        return TypeName.get(
                environment.getElementUtils().getTypeElement(
                        fullPathName.apply(basePackage, contentClassStr)
                ).asType());
    }

    private String containerClassStr;

    public TypeName containerClassName() {
        return TypeName.get(
                environment.getElementUtils().getTypeElement(
                        fullPathName.apply(basePackage, containerClassStr)
                ).asType());
    }

    private TypeName letter;
    private TypeName email;
    private TypeName sms;

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public Tuple2<String, String> getCodePair() {
        return codePair;
    }

    public void setCodePair(Tuple2<String, String> codePair) {
        this.codePair = codePair;
    }

    public TypeName getLetter() {
        return letter;
    }

    public void setLetter(TypeName letter) {
        this.letter = letter;
    }

    public TypeName getEmail() {
        return email;
    }

    public void setEmail(TypeName email) {
        this.email = email;
    }

    public TypeName getSms() {
        return sms;
    }

    public void setSms(TypeName sms) {
        this.sms = sms;
    }

    public String getContentClassStr() {
        return contentClassStr;
    }

    public void setContentClassStr(String contentClassStr) {
        this.contentClassStr = contentClassStr;
    }
    public String fullContentClassStr() {
        return String.format("%s.%s", basePackage, contentClassStr);
    }

    public String fullContainerClassStr() {
        return String.format("%s.%s", basePackage, containerClassStr);
    }

    public String getContainerClassStr() {
        return containerClassStr;
    }


    public void setContainerClassStr(String containerClassStr) {
        this.containerClassStr = containerClassStr;
    }

    public ProcessingEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(ProcessingEnvironment environment) {
        this.environment = environment;
    }
}
