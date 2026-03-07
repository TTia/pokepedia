package com.ttia.pokepedia.funtranslations;

import com.ttia.pokepedia.funtranslations.model.TranslationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FunTranslationsClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private FunTranslationsClient funTranslationsClient;

    @Test
    void returnsTranslatedTextOnSuccess() {
        stubRestClientChain(new TranslationResponse(new TranslationResponse.Contents("Translated text, this is.")));

        Optional<String> result = funTranslationsClient.translate("Some text", TranslationType.YODA);

        assertThat(result).contains("Translated text, this is.");
    }

    @Test
    void returnsEmptyOnRestClientException() {
        when(restClient.get()).thenThrow(new RestClientException("Rate limited"));

        Optional<String> result = funTranslationsClient.translate("Some text", TranslationType.SHAKESPEARE);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyOnNullBody() {
        stubRestClientChain(null);

        Optional<String> result = funTranslationsClient.translate("Some text", TranslationType.YODA);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyOnNullContents() {
        stubRestClientChain(new TranslationResponse(null));

        Optional<String> result = funTranslationsClient.translate("Some text", TranslationType.YODA);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyOnBlankTranslatedText() {
        stubRestClientChain(new TranslationResponse(new TranslationResponse.Contents("")));

        Optional<String> result = funTranslationsClient.translate("Some text", TranslationType.YODA);

        assertThat(result).isEmpty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubRestClientChain(TranslationResponse response) {
        RestClient.RequestHeadersUriSpec rawUriSpec = requestHeadersUriSpec;
        RestClient.RequestHeadersSpec rawHeadersSpec = requestHeadersSpec;
        when(restClient.get()).thenReturn(rawUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(), any())).thenReturn(rawHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(eq(TranslationResponse.class))).thenReturn(response);
    }
}
