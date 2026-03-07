package com.ttia.pokepedia.pokemon;

import com.ttia.pokepedia.funtranslations.FunTranslationsClient;
import com.ttia.pokepedia.funtranslations.TranslationType;
import com.ttia.pokepedia.pokeapi.PokeApiClient;
import com.ttia.pokepedia.pokeapi.model.NamedAPIResource;
import com.ttia.pokepedia.pokeapi.model.PokemonSpeciesDetail;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@Service
public class PokemonService {

    private static final String CAVE_HABITAT = "cave";

    private final PokeApiClient pokeApiClient;
    private final FunTranslationsClient funTranslationsClient;

    public PokemonService(PokeApiClient pokeApiClient, FunTranslationsClient funTranslationsClient) {
        this.pokeApiClient = pokeApiClient;
        this.funTranslationsClient = funTranslationsClient;
    }

    public PokemonResponse getPokemon(String name) {
        PokemonSpeciesDetail species;
        try {
            species = pokeApiClient.getPokemonSpecies(name);
        } catch (HttpClientErrorException.NotFound e) {
            throw new PokemonNotFoundException(name);
        }

        String description = species.getFlavorTextEntries().stream()
                .filter(entry -> "en".equals(entry.getLanguage().getName()))
                .findFirst()
                .map(entry -> entry.getFlavorText()
                        .replaceAll("[\\n\\f\\r]", " ")
                        .replaceAll(" +", " "))
                .map(StringUtils::trimWhitespace)
                .orElse("");

        String habitat = Optional.ofNullable(species.getHabitat())
                .map(NamedAPIResource::getName)
                .orElse(null);

        return new PokemonResponse(species.getName(), description, habitat, species.getIsLegendary());
    }

    public PokemonResponse getTranslatedPokemon(String name) {
        PokemonResponse base = getPokemon(name);

        if (base.description().isEmpty()) {
            return base;
        }

        TranslationType type = CAVE_HABITAT.equals(base.habitat()) || base.isLegendary()
                ? TranslationType.YODA
                : TranslationType.SHAKESPEARE;

        String description = funTranslationsClient.translate(base.description(), type)
                .orElse(base.description());

        return new PokemonResponse(base.name(), description, base.habitat(), base.isLegendary());
    }
}
