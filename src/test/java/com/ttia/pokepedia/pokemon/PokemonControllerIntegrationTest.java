package com.ttia.pokepedia.pokemon;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class PokemonControllerIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

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
}
