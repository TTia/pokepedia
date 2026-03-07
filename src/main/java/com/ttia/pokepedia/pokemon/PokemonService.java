package com.ttia.pokepedia.pokemon;

import com.ttia.pokepedia.pokeapi.PokeApiClient;
import com.ttia.pokepedia.pokeapi.model.NamedAPIResource;
import com.ttia.pokepedia.pokeapi.model.PokemonSpeciesDetail;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@Service
public class PokemonService {

    private final PokeApiClient pokeApiClient;

    public PokemonService(PokeApiClient pokeApiClient) {
        this.pokeApiClient = pokeApiClient;
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
}
