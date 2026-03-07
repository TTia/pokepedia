package com.ttia.pokepedia.pokemon;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pokemon information")
public record PokemonResponse(
        @Schema(description = "Pokemon name", example = "mewtwo") String name,
        @Schema(description = "Pokemon description from the Pokedex", example = "It was created by a scientist after years of horrific gene splicing and DNA engineering experiments.") String description,
        @Schema(description = "Pokemon habitat", example = "rare") String habitat,
        @Schema(description = "Whether the Pokemon is legendary", example = "true") boolean isLegendary) {
}
