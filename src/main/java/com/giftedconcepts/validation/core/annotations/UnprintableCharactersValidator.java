package com.giftedconcepts.validation.core.annotations;

import com.giftedconcepts.validation.core.config.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

@Slf4j
public class UnprintableCharactersValidator implements ConstraintValidator<UnprintableCharacters, Object> {

    private static final String REGEX_ANY_CHARACTER = "(.*)";
    public static final String FIELD_HAS_FAILED_VALIDATION = "Field %s has special characters which failed validation.";

    private String identifierField;
    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private List<Character> allowedLegalCharacters;

    @Override
    public void initialize(UnprintableCharacters constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        identifierField = constraintAnnotation.identifierField();
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
                    boolean isLegalField = isAnnotatedBy(field.getAnnotation(UnprintableCharactersAllowLegal.class));
                    boolean isSkippableField = isAnnotatedBy(field.getAnnotation(UnprintableCharactersSkip.class));
                    log.info("Is this String marked as legal content {}", isLegalField);
                    boolean isValidCheck;
                    if (isSkippableField){
                        isValidCheck = true;
                    } else if(isLegalField){
                        isValidCheck = handleLegalValidation(field, context, object);
                    } else {
                        isValidCheck = handleBaseValidation(field, context, object);
                    }
                    isValid = isValidCheck && isValid;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            isValid = false;
        }

        return isValid;
    }

    private static void createInvalidMessage(final Field field,
                                             final ConstraintValidatorContext constraintValidatorContext,
                                             final String failureMessage) {

        String declaringClass = StringUtils.substringAfterLast(String.valueOf(field.getDeclaringClass()), ".");
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext.buildConstraintViolationWithTemplate(failureMessage)
                .addPropertyNode(declaringClass + "." + field.getName()).addConstraintViolation();
    }

    /**
     *
     * @param field   String.class Field in which the validation should be performed
     * @param constraintValidatorContext The ConstraintValidatorContext will be needed if the field fails
     * @param object The object for which the Annotation has been applied to.
     * @return isValid boolean based on if the field passes validation or not.
     * @throws IllegalAccessException
     */
    private boolean handleLegalValidation(final Field field, final ConstraintValidatorContext constraintValidatorContext,
                                          final Object object) throws IllegalAccessException {
        boolean isValid = true;
        String stringValue = (String)field.get(object);
        log.info("Legal Content - Field Name {} Field Value {}.", field.getName(), stringValue);

        if(stringValue != null){
            for(int i = 0; i < stringValue.length(); ++i){
                char c = stringValue.charAt(i);
                if(!(c >= ' ' && c < 127) && !allowedLegalCharacters.contains(c)){
                    String failureMessage = String.format(FIELD_HAS_FAILED_VALIDATION, field.getName());
                    log.info(failureMessage);
                    /* ignore the noise from IntelliJ about this always being false yes it is if the code makes it here. */
                    isValid = isValid && false;
                    log.debug("Invalid character {}", c);
                    createInvalidMessage(field, constraintValidatorContext, failureMessage);
                }
            }
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
        if(stringValue != null &&
                stringValue.matches(REGEX_ANY_CHARACTER + applicationProperties.getUnprintableRegex()
                        + REGEX_ANY_CHARACTER)){
            String failureMessage = String.format(FIELD_HAS_FAILED_VALIDATION, field.getName());
            log.info(failureMessage);
            isValid = false;
            createInvalidMessage(field, constraintValidatorContext, failureMessage);
        }

        return isValid;
    }

    private boolean isAnnotatedBy(final Annotation annotation) {
        boolean isAnnotatedField = annotation != null;

        return isAnnotatedField;
    }
}
