package com.ttia.pokepedia.funtranslations;

import com.ttia.pokepedia.funtranslations.model.TranslationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Service
public class FunTranslationsClient {

    private static final Logger log = LoggerFactory.getLogger(FunTranslationsClient.class);

    private final RestClient restClient;

    public FunTranslationsClient(RestClient funTranslationsRestClient) {
        this.restClient = funTranslationsRestClient;
    }

    @Cacheable(value = "translations", unless = "#result.isEmpty()")
    public Optional<String> translate(String text, TranslationType type) {
        try {
            TranslationResponse response = restClient.get()
                    .uri("/{type}.json?text={text}", type.getPathSegment(), text)
                    .retrieve()
                    .body(TranslationResponse.class);

            if (response == null || response.contents() == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.contents().translated())
                    .filter(t -> !t.isBlank());
        } catch (RestClientException e) {
            log.warn("Translation failed for type {}: verify the throttling", type);
            return Optional.empty();
        }
    }
}
