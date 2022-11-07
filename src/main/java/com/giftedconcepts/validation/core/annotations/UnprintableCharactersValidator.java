package com.giftedconcepts.validation.core.annotations;

import com.giftedconcepts.validation.core.config.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

@Slf4j
public class UnprintableCharactersValidator implements ConstraintValidator<UnprintableCharacters, Object> {

    private static final String REGEX_ANY_CHARACTER = "(.*)";
    public static final String FIELD_HAS_FAILED_VALIDATION = "Field %s has special characters which failed validation.";
    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public void initialize(UnprintableCharacters constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {

        boolean isValid = true;

        try {
            /* get the class of the annotated object */
            Class<?> clazz = object.getClass();
            /* Get the list of fields in the Object */
            Field[] allFields = clazz.getDeclaredFields();
            for(Field field: allFields){
                /* Setting the fields as accessible in order to view the metadata */
                field.setAccessible(true);
                /* We only care about String.class for this validation */
                if(field.getType().equals(String.class)){
                    /* There may be some fields that have legal implications and they should be marked by the Legal annotation */
                    boolean isLegalField = isLegalOnly(field.getAnnotation(UnprintableCharactersAllowLegal.class));
                    log.info("Is this String marked as legal content {}", isLegalField);
                    if(isLegalField){

                    } else {
                        isValid = isValid && handleBaseValidation(field, context, object);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            isValid = false;
        }

        return isValid;
    }

    /**
     *
     * @param field   String.class Field in which the validation should be performed
     * @param constraintValidatorContext The ConstraintValidatorContext will be needed if the field fails
     * @param object The object for which the Annotation has been applied to.
     * @return isValid boolean based on if the field passes validation or not.
     * @throws IllegalAccessException
     */
    private boolean handleBaseValidation(final Field field, final ConstraintValidatorContext constraintValidatorContext,
                                      final Object object) throws IllegalAccessException {
        boolean isValid = true;
        String stringValue = (String)field.get(object);
        log.info("Field Name {} Field Value {}.", field.getName(), stringValue);
        if(stringValue.matches(REGEX_ANY_CHARACTER + applicationProperties.getUnprintableRegex() + REGEX_ANY_CHARACTER)){
            String failureMessage = String.format(FIELD_HAS_FAILED_VALIDATION, field.getName());
            log.info(failureMessage);
            isValid = false;
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(failureMessage)
                    .addPropertyNode(field.getName()).addConstraintViolation();
        }

        return isValid;
    }

    private boolean isLegalOnly(final Annotation annotation) {
        boolean isLegalField = false;

        if(annotation != null){
            isLegalField = true;
        }

        return isLegalField;
    }
}
