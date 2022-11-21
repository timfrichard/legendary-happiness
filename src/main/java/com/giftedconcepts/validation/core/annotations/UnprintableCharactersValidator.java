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

    public static final String FIELD_HAS_FAILED_VALIDATION_MSG = "Field %s has special characters which failed validation.";
    public static final String IDENTIFIER_FIELD_MSG = "The identifying field value: %s";
    private static final String REGEX_ANY_CHARACTER = "(.*)";
    private String[] identifierFields;
    private String identifierFieldsMessage;
    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private List<Character> allowedLegalCharacters;

    @Override
    public void initialize(final UnprintableCharacters constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        identifierFields = constraintAnnotation.identifierFields();
        identifierFieldsMessage = constraintAnnotation.identifierFieldsMessage();
    }

    @Override
    public boolean isValid(final Object object, final ConstraintValidatorContext context) {

        boolean isValid = true;

        try {
            /* get the class of the annotated object */
            final Class<?> clazz = object.getClass();
            /* Get the list of fields in the Object */
            final Field[] allFields = clazz.getDeclaredFields();
            for (final Field field : allFields) {
                /* Setting the fields as accessible in order to view the metadata */
                field.setAccessible(true);
                /* We only care about String.class for this validation */
                if (field.getType().equals(String.class)) {
                    final boolean isValidCheck;
                    if (isAnnotatedBy(field.getAnnotation(UnprintableCharactersSkip.class))) {
                        /* Skippable */
                        isValidCheck = true;
                    } else if (isAnnotatedBy(field.getAnnotation(UnprintableCharactersAllowLegal.class))) {
                        /* Fields that have legal implications should be marked by the Legal annotation */
                        isValidCheck = handleLegalValidation(field, context, object);
                    } else {
                        isValidCheck = handleBaseValidation(field, context, object);
                    }
                    isValid = isValidCheck && isValid;
                }
            }
        } catch (final Exception e) {
            log.error(e.getMessage());
            isValid = false;
        }

        return isValid;
    }

    private void createInvalidMessage(final ConstraintValidatorContext constraintValidatorContext,
                                      final String failureMessage, final String propertyNode, final Object object)
            throws NoSuchFieldException, IllegalAccessException {
        String updatedFailureMsg = failureMessage;

        if (identifierFields != null && identifierFields.length > 0) {
            updatedFailureMsg = appendInvalidMessageWithArrayOfIdentifiers(failureMessage, object);
        }

        log.info("The failure message {}, for property {}.", updatedFailureMsg, propertyNode);
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext.buildConstraintViolationWithTemplate(updatedFailureMsg)
                .addPropertyNode(propertyNode).addConstraintViolation();
    }

    private String appendInvalidMessageWithArrayOfIdentifiers(final String failureMessage, final Object object)
            throws NoSuchFieldException, IllegalAccessException {

        final String updatedFailureMsg;
        final Object[] fields = new Object[identifierFields.length];
        for (int i = 0; i < identifierFields.length; ++i) {
            /* Get Field which identifies what failed */
            final Field identifyingField = object.getClass().getDeclaredField(identifierFields[i]);
            /* Get Field which identifies what failed */
            identifyingField.setAccessible(true);
            /* Add the field value to the Object array */
            fields[i] = identifyingField.get(object).toString();
        }

        updatedFailureMsg = failureMessage + " " + String.format(identifierFieldsMessage, fields);
        return updatedFailureMsg;
    }

    private String createPropertyNode(final Field field) {

        final String declaringClass = StringUtils.substringAfterLast(String.valueOf(field.getDeclaringClass()), ".");
        return declaringClass + "." + field.getName();
    }

    /**
     * @param field                      String.class Field in which the validation should be performed
     * @param constraintValidatorContext The ConstraintValidatorContext will be needed if the field fails
     * @param object                     The object for which the Annotation has been applied to.
     * @return isValid boolean based on if the field passes validation or not.
     * @throws IllegalAccessException
     */
    private boolean handleLegalValidation(final Field field, final ConstraintValidatorContext constraintValidatorContext,
                                          final Object object) throws IllegalAccessException, NoSuchFieldException {
        boolean isValid = true;
        final String stringValue = (String) field.get(object);
        log.debug("Legal Content - Field Name {} Field Value {}.", field.getName(), stringValue);

        if (stringValue != null) {
            for (int i = 0; i < stringValue.length(); ++i) {
                final char c = stringValue.charAt(i);
                if (!(c >= ' ' && c < 127) && !allowedLegalCharacters.contains(c)) {
                    /* ignore the noise from IntelliJ about this always being false yes it is if the code makes it here. */
                    isValid = isValid && false;
                    log.info("Invalid character {}", c);
                    final String propertyNode = createPropertyNode(field);
                    final String failureMessage = String.format(FIELD_HAS_FAILED_VALIDATION_MSG, propertyNode);
                    createInvalidMessage(constraintValidatorContext, failureMessage, propertyNode, object);
                }
            }
        }

        return isValid;
    }

    /**
     * @param field                      String.class Field in which the validation should be performed
     * @param constraintValidatorContext The ConstraintValidatorContext will be needed if the field fails
     * @param object                     The object for which the Annotation has been applied to.
     * @return isValid boolean based on if the field passes validation or not.
     * @throws IllegalAccessException
     */
    private boolean handleBaseValidation(final Field field, final ConstraintValidatorContext constraintValidatorContext,
                                         final Object object) throws IllegalAccessException, NoSuchFieldException {
        boolean isValid = true;
        final String stringValue = (String) field.get(object);
        log.debug("Field Name {} Field Value {}.", field.getName(), stringValue);
        if (stringValue != null &&
                stringValue.matches(REGEX_ANY_CHARACTER + applicationProperties.getUnprintableRegex()
                        + REGEX_ANY_CHARACTER)) {
            isValid = false;
            log.info("Invalid value {}", stringValue);
            final String propertyNode = createPropertyNode(field);
            final String failureMessage = String.format(FIELD_HAS_FAILED_VALIDATION_MSG, propertyNode);
            createInvalidMessage(constraintValidatorContext, failureMessage, propertyNode, object);
        }

        return isValid;
    }

    private boolean isAnnotatedBy(final Annotation annotation) {
        final boolean isAnnotatedField = annotation != null;

        return isAnnotatedField;
    }
}
