package com.ttia.pokepedia.funtranslations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class FunTranslationsClientConfig {

    @Bean
    RestClient funTranslationsRestClient(@Value("${funtranslations.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
