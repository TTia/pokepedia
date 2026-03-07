package com.ttia.pokepedia.pokemon;

import com.ttia.pokepedia.funtranslations.FunTranslationsClient;
import com.ttia.pokepedia.funtranslations.TranslationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class PokemonControllerIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private FunTranslationsClient funTranslationsClient;

    @Test
    void fetchesMewtwo() {
        restTestClient.get().uri("/pokemon/mewtwo").exchange()
                .expectStatus().isOk()
                .expectBody(PokemonResponse.class).value(body -> {
                    assert body.name().equals("mewtwo");
                    assert body.isLegendary();
                    assert "rare".equals(body.habitat());
                    assert !body.description().isEmpty();
                });
    }

    @Test
    void fetchesMrMime() {
        restTestClient.get().uri("/pokemon/mr-mime").exchange()
                .expectStatus().isOk()
                .expectBody(PokemonResponse.class).value(body -> {
                    assert body.name().equals("mr-mime");
                    assert !body.isLegendary();
                    assert !body.description().isEmpty();
                });
    }

    @Test
    void returnsNotFoundForNonExistentPokemon() {
        restTestClient.get().uri("/pokemon/notapokemon").exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.error").isEqualTo("Pokemon not found: \"notapokemon\"");
    }

    @Test
    void returnsBadRequestForInvalidCharacters() {
        restTestClient.get().uri("/pokemon/<script>").exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void fetchesTranslatedMewtwo() {
        when(funTranslationsClient.translate(anyString(), eq(TranslationType.YODA)))
                .thenReturn(Optional.of("Created by a scientist, it was."));

        restTestClient.get().uri("/pokemon/translated/mewtwo").exchange()
                .expectStatus().isOk()
                .expectBody(PokemonResponse.class).value(body -> {
                    assert body.name().equals("mewtwo");
                    assert body.isLegendary();
                    assert "rare".equals(body.habitat());
                    assert "Created by a scientist, it was.".equals(body.description());
                });

        verify(funTranslationsClient).translate(anyString(), eq(TranslationType.YODA));
    }

    @Test
    void fetchesZubatWithCaveHabitat() {
        restTestClient.get().uri("/pokemon/zubat").exchange()
                .expectStatus().isOk()
                .expectBody(PokemonResponse.class).value(body -> {
                    assert body.name().equals("zubat");
                    assert !body.isLegendary();
                    assert "cave".equals(body.habitat());
                    assert !body.description().isEmpty();
                });
    }

    @Test
    void translatesZubatWithYodaDueToCaveHabitat() {
        when(funTranslationsClient.translate(anyString(), eq(TranslationType.YODA)))
                .thenReturn(Optional.of("Forms colonies in perpetually dark places, it does."));

        restTestClient.get().uri("/pokemon/translated/zubat").exchange()
                .expectStatus().isOk()
                .expectBody(PokemonResponse.class).value(body -> {
                    assert body.name().equals("zubat");
                    assert !body.isLegendary();
                    assert "cave".equals(body.habitat());
                    assert "Forms colonies in perpetually dark places, it does.".equals(body.description());
                });

        verify(funTranslationsClient).translate(anyString(), eq(TranslationType.YODA));
    }

    @Test
    void translatesPikachuWithShakespeare() {
        when(funTranslationsClient.translate(anyString(), eq(TranslationType.SHAKESPEARE)))
                .thenReturn(Optional.of("At which hour several of these pokemon gather, verily."));

        restTestClient.get().uri("/pokemon/translated/pikachu").exchange()
                .expectStatus().isOk()
                .expectBody(PokemonResponse.class).value(body -> {
                    assert body.name().equals("pikachu");
                    assert !body.isLegendary();
                    assert "forest".equals(body.habitat());
                    assert "At which hour several of these pokemon gather, verily.".equals(body.description());
                });

        verify(funTranslationsClient).translate(anyString(), eq(TranslationType.SHAKESPEARE));
    }
}
