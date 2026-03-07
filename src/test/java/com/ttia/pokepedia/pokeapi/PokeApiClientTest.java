package com.ttia.pokepedia.pokeapi;

import com.ttia.pokepedia.pokeapi.model.PokemonSpeciesDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PokeApiClientTest {

    @Autowired
    private PokeApiClient pokeApiClient;

    @Test
    void fetchesMewtwoFromPokeApi() {
        PokemonSpeciesDetail result = pokeApiClient.getPokemonSpecies("mewtwo");

        assertThat(result.getName()).isEqualTo("mewtwo");
        assertThat(result.getIsLegendary()).isTrue();
        assertThat(result.getHabitat()).isNotNull();
        assertThat(result.getHabitat().getName()).isEqualTo("rare");
        assertThat(result.getFlavorTextEntries()).isNotEmpty();
    }

    @Test
    void fetchesMrMimeFromPokeApi() {
        PokemonSpeciesDetail result = pokeApiClient.getPokemonSpecies("mr-mime");

        assertThat(result.getName()).isEqualTo("mr-mime");
        assertThat(result.getIsLegendary()).isFalse();
        assertThat(result.getFlavorTextEntries()).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"mr. mime", "mr mime"})
    void rejectsNonNormalizedNames(String name) {
        assertThatThrownBy(() -> pokeApiClient.getPokemonSpecies(name))
                .isInstanceOf(HttpClientErrorException.class);
    }

    @Test
    void throwsNotFoundForNonExistentPokemon() {
        assertThatThrownBy(() -> pokeApiClient.getPokemonSpecies("not-a-pokemon"))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }
}
