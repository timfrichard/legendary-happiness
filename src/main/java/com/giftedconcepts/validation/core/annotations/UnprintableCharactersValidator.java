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
    public static final String FIELD_HAS_FAILED_VALIDATION_MSG = "Field %s has special characters which failed validation.";
    public static final String IDENTIFIER_FIELD_MSG = "The identifying field value: %s";

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

    private void createInvalidMessage(final ConstraintValidatorContext constraintValidatorContext,
                                      final String failureMessage, final String propertyNode, Object object)
            throws NoSuchFieldException, IllegalAccessException {
        String updatedFailureMsg = failureMessage;

        if (!StringUtils.isBlank(identifierField)) {
            /* Get Field which identifies what failed */
            Field identifyingField = object.getClass().getDeclaredField(identifierField);
            /* Set accessibility to true */
            identifyingField.setAccessible(true);
            /* This should be a string value */
            String identifyingMessage = identifyingField.get(object).toString();

            updatedFailureMsg = failureMessage + " " + String.format(IDENTIFIER_FIELD_MSG, identifyingMessage);
        }

        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext.buildConstraintViolationWithTemplate(updatedFailureMsg)
                .addPropertyNode(propertyNode).addConstraintViolation();
    }

    private String createPropertyNode(final Field field) {

        String declaringClass = StringUtils.substringAfterLast(String.valueOf(field.getDeclaringClass()), ".");
        return declaringClass + "." + field.getName();
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
                                          final Object object) throws IllegalAccessException, NoSuchFieldException {
        boolean isValid = true;
        String stringValue = (String)field.get(object);
        log.info("Legal Content - Field Name {} Field Value {}.", field.getName(), stringValue);

        if(stringValue != null){
            for(int i = 0; i < stringValue.length(); ++i){
                char c = stringValue.charAt(i);
                if(!(c >= ' ' && c < 127) && !allowedLegalCharacters.contains(c)){
                    /* ignore the noise from IntelliJ about this always being false yes it is if the code makes it here. */
                    isValid = isValid && false;
                    log.debug("Invalid character {}", c);
                    String propertyNode = createPropertyNode(field);
                    String failureMessage = String.format(FIELD_HAS_FAILED_VALIDATION_MSG, propertyNode);
                    log.info(failureMessage);
                    createInvalidMessage(constraintValidatorContext, failureMessage, propertyNode, object);
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
                                      final Object object) throws IllegalAccessException, NoSuchFieldException {
        boolean isValid = true;
        String stringValue = (String)field.get(object);
        log.info("Field Name {} Field Value {}.", field.getName(), stringValue);
        if(stringValue != null &&
                stringValue.matches(REGEX_ANY_CHARACTER + applicationProperties.getUnprintableRegex()
                        + REGEX_ANY_CHARACTER)){
            isValid = false;
            String propertyNode = createPropertyNode(field);
            String failureMessage = String.format(FIELD_HAS_FAILED_VALIDATION_MSG, propertyNode);
            log.info(failureMessage);
            createInvalidMessage(constraintValidatorContext, failureMessage, propertyNode, object);
        }

        return isValid;
    }

    private boolean isAnnotatedBy(final Annotation annotation) {
        boolean isAnnotatedField = annotation != null;

        return isAnnotatedField;
    }
}
