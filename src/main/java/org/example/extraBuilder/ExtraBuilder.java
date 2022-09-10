package org.example.extraBuilder;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Repeatable(ExtraBuilderContainer.class)
@Retention(RetentionPolicy.SOURCE)
public @interface ExtraBuilder {
    String code();
    Class<?> letter();
    Class<?> email();
    Class<?> sms();
}
