package com.ttia.pokepedia.pokemon;

import com.ttia.pokepedia.pokeapi.PokeApiClient;
import com.ttia.pokepedia.pokeapi.model.NamedAPIResource;
import com.ttia.pokepedia.pokeapi.model.PokemonSpeciesDetail;
import com.ttia.pokepedia.pokeapi.model.PokemonSpeciesFlavorText;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonServiceTest {

    @Mock
    private PokeApiClient pokeApiClient;

    @InjectMocks
    private PokemonService pokemonService;

    @Test
    void mapsAllFieldsCorrectly() {
        PokemonSpeciesDetail species = buildSpecies("mewtwo", true, "rare",
                List.of(flavorText("It was created by\na scientist after\nyears of horrific\ngene splicing and\nDNA engineering\nexperiments.", "en")));

        when(pokeApiClient.getPokemonSpecies("mewtwo")).thenReturn(species);

        PokemonResponse response = pokemonService.getPokemon("mewtwo");

        assertThat(response.name()).isEqualTo("mewtwo");
        assertThat(response.description()).isEqualTo("It was created by a scientist after years of horrific gene splicing and DNA engineering experiments.");
        assertThat(response.habitat()).isEqualTo("rare");
        assertThat(response.isLegendary()).isTrue();
    }

    @Test
    void cleansFormFeedsAndCollapseSpaces() {
        PokemonSpeciesDetail species = buildSpecies("bulbasaur", false, "grassland",
                List.of(flavorText("A strange\fseed was\fplanted on\nits  back  at birth.", "en")));

        when(pokeApiClient.getPokemonSpecies("bulbasaur")).thenReturn(species);

        PokemonResponse response = pokemonService.getPokemon("bulbasaur");

        assertThat(response.description()).isEqualTo("A strange seed was planted on its back at birth.");
    }

    @Test
    void returnsNullHabitatWhenMissing() {
        PokemonSpeciesDetail species = buildSpecies("deoxys", true, null,
                List.of(flavorText("An alien virus.", "en")));

        when(pokeApiClient.getPokemonSpecies("deoxys")).thenReturn(species);

        PokemonResponse response = pokemonService.getPokemon("deoxys");

        assertThat(response.habitat()).isNull();
    }

    @Test
    void returnsEmptyDescriptionWhenNoEnglishEntry() {
        PokemonSpeciesDetail species = buildSpecies("pikachu", false, "forest",
                List.of(flavorText("Quand plusieurs de ces Pokemon se reunissent", "fr")));

        when(pokeApiClient.getPokemonSpecies("pikachu")).thenReturn(species);

        PokemonResponse response = pokemonService.getPokemon("pikachu");

        assertThat(response.description()).isEmpty();
    }

    @Test
    void throwsPokemonNotFoundWhenApiReturns404() {
        when(pokeApiClient.getPokemonSpecies("notapokemon"))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> pokemonService.getPokemon("notapokemon"))
                .isInstanceOf(PokemonNotFoundException.class)
                .hasMessageContaining("notapokemon");
    }

    private static PokemonSpeciesDetail buildSpecies(String name, boolean legendary, String habitat,
                                                     List<PokemonSpeciesFlavorText> flavorTexts) {
        PokemonSpeciesDetail species = new PokemonSpeciesDetail();
        species.setName(name);
        species.setIsLegendary(legendary);
        if (habitat != null) {
            NamedAPIResource habitatResource = new NamedAPIResource();
            habitatResource.setName(habitat);
            species.setHabitat(habitatResource);
        }
        species.setFlavorTextEntries(flavorTexts);
        return species;
    }

    private static PokemonSpeciesFlavorText flavorText(String text, String lang) {
        PokemonSpeciesFlavorText entry = new PokemonSpeciesFlavorText();
        entry.setFlavorText(text);
        NamedAPIResource language = new NamedAPIResource();
        language.setName(lang);
        entry.setLanguage(language);
        return entry;
    }
}
