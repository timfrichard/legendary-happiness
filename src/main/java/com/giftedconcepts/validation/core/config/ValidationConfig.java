package com.giftedconcepts.validation.core.config;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ValidationConfig {

    @Bean
    public List<Character> allowedLegalCharacters(final ApplicationProperties applicationProperties){
        List<Character> characters = Lists.newArrayList();

        Arrays.stream(applicationProperties.getAllowedLegalValidationCharacters()).forEach(s -> characters.add((char)s));

        return  characters;
    }
}
