package com.giftedconcepts.validation.core.annotations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UnprintableCharactersValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UnprintableCharacters {

    Class<?>[] groups() default {};

    String message() default "No message provided";

    Class<? extends Payload>[] payload() default {};

    String[] identifierFields() default {};

    String identifierFieldsMessage() default "";
}
