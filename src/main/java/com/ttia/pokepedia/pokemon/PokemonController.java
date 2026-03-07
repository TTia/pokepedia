package com.ttia.pokepedia.pokemon;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/pokemon")
@Tag(name = "Pokemon", description = "Pokemon information endpoints")
public class PokemonController {

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping("/{name}")
    @Operation(
            summary = "Get basic Pokemon information",
            description = "Returns name, description, habitat and legendary status for the given Pokemon")
    @ApiResponse(responseCode = "200", description = "Pokemon found")
    @ApiResponse(responseCode = "404", description = "Pokemon not found")
    @ApiResponse(responseCode = "400", description = "Invalid Pokemon name")
    public PokemonResponse getPokemon(
            @PathVariable
            @Parameter(description = "Pokemon name (e.g. mewtwo, mr-mime)")
            @Pattern(regexp = "[a-zA-Z0-9-]+") @Size(max = 50) String name) {
        return pokemonService.getPokemon(name.toLowerCase());
    }

    @GetMapping("/translated/{name}")
    @Operation(
            summary = "Get Pokemon information with translated description",
            description = "Returns Pokemon info with a fun translation: Yoda for cave/legendary Pokemon, Shakespeare otherwise. Falls back to standard description if translation fails.")
    @ApiResponse(responseCode = "200", description = "Pokemon found with translated description")
    @ApiResponse(responseCode = "404", description = "Pokemon not found")
    @ApiResponse(responseCode = "400", description = "Invalid Pokemon name")
    public PokemonResponse getTranslatedPokemon(
            @PathVariable
            @Parameter(description = "Pokemon name (e.g. mewtwo, mr-mime)")
            @Pattern(regexp = "[a-zA-Z0-9-]+") @Size(max = 50) String name) {
        return pokemonService.getTranslatedPokemon(name.toLowerCase());
    }
}
