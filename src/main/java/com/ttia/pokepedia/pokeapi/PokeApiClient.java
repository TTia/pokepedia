package com.ttia.pokepedia.pokeapi;

import com.ttia.pokepedia.pokeapi.model.PokemonSpeciesDetail;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PokeApiClient {

    private final RestClient restClient;

    public PokeApiClient(RestClient pokeApiRestClient) {
        this.restClient = pokeApiRestClient;
    }

    @Cacheable("pokemon-species")
    public PokemonSpeciesDetail getPokemonSpecies(String name) {
        return restClient.get()
                .uri("/pokemon-species/{name}", name)
                .retrieve()
                .body(PokemonSpeciesDetail.class);
    }
}
