package com.ttia.pokepedia.pokemon;

import com.ttia.pokepedia.funtranslations.FunTranslationsClient;
import com.ttia.pokepedia.funtranslations.TranslationType;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonServiceTest {

    @Mock
    private PokeApiClient pokeApiClient;

    @Mock
    private FunTranslationsClient funTranslationsClient;

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

    @Test
    void translatesWithYodaForCaveHabitat() {
        stubSpecies("zubat", false, "cave", "A common Pokemon in caves.");
        when(funTranslationsClient.translate("A common Pokemon in caves.", TranslationType.YODA))
                .thenReturn(Optional.of("In caves, a common Pokemon, this is."));

        PokemonResponse response = pokemonService.getTranslatedPokemon("zubat");

        assertThat(response.description()).isEqualTo("In caves, a common Pokemon, this is.");
        verify(funTranslationsClient).translate("A common Pokemon in caves.", TranslationType.YODA);
    }

    @Test
    void translatesWithYodaForLegendary() {
        stubSpecies("mewtwo", true, "rare", "A Pokemon created by science.");
        when(funTranslationsClient.translate("A Pokemon created by science.", TranslationType.YODA))
                .thenReturn(Optional.of("By science, a Pokemon created was."));

        PokemonResponse response = pokemonService.getTranslatedPokemon("mewtwo");

        assertThat(response.description()).isEqualTo("By science, a Pokemon created was.");
        verify(funTranslationsClient).translate("A Pokemon created by science.", TranslationType.YODA);
    }

    @Test
    void translatesWithShakespeareForStandardPokemon() {
        stubSpecies("pikachu", false, "forest", "An electric mouse Pokemon.");
        when(funTranslationsClient.translate("An electric mouse Pokemon.", TranslationType.SHAKESPEARE))
                .thenReturn(Optional.of("An electric mouse Pokemon, verily."));

        PokemonResponse response = pokemonService.getTranslatedPokemon("pikachu");

        assertThat(response.description()).isEqualTo("An electric mouse Pokemon, verily.");
        verify(funTranslationsClient).translate("An electric mouse Pokemon.", TranslationType.SHAKESPEARE);
    }

    @Test
    void fallsBackToStandardDescriptionOnTranslationFailure() {
        stubSpecies("pikachu", false, "forest", "An electric mouse Pokemon.");
        when(funTranslationsClient.translate(anyString(), eq(TranslationType.SHAKESPEARE)))
                .thenReturn(Optional.empty());

        PokemonResponse response = pokemonService.getTranslatedPokemon("pikachu");

        assertThat(response.description()).isEqualTo("An electric mouse Pokemon.");
    }

    @Test
    void skipsTranslationForEmptyDescription() {
        stubSpecies("pikachu", false, "forest", "");

        PokemonResponse response = pokemonService.getTranslatedPokemon("pikachu");

        assertThat(response.description()).isEmpty();
    }

    private void stubSpecies(String name, boolean legendary, String habitat, String description) {
        String lang = description.isEmpty() ? "fr" : "en";
        PokemonSpeciesDetail species = buildSpecies(name, legendary, habitat,
                List.of(flavorText(description.isEmpty() ? "Non-English text" : description, lang)));
        when(pokeApiClient.getPokemonSpecies(name)).thenReturn(species);
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
