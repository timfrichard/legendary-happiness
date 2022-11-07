package com.giftedconcepts.validation.core.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "com.giftedconcepts.validate.annotation")
@ToString
public class ApplicationProperties {

    private String unprintableRegex;
    private String[] legalValidation;

}