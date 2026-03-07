package com.ttia.pokepedia.pokemon;

public class PokemonNotFoundException extends RuntimeException {

    public PokemonNotFoundException(String name) {
        super("Pokemon not found: \"" + name + "\"");
    }
}
