package com.ttia.pokepedia.pokemon;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PokemonController.class, excludeAutoConfiguration = CacheAutoConfiguration.class)
class PokemonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PokemonService pokemonService;

    @Test
    void returnsOkWithCorrectJsonShape() throws Exception {
        when(pokemonService.getPokemon("mewtwo"))
                .thenReturn(new PokemonResponse("mewtwo", "A Pokemon created by science.", "rare", true));

        mockMvc.perform(get("/pokemon/mewtwo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("mewtwo"))
                .andExpect(jsonPath("$.description").value("A Pokemon created by science."))
                .andExpect(jsonPath("$.habitat").value("rare"))
                .andExpect(jsonPath("$.isLegendary").value(true));
    }

    @Test
    void returnsNotFoundWhenPokemonDoesNotExist() throws Exception {
        when(pokemonService.getPokemon("notapokemon"))
                .thenThrow(new PokemonNotFoundException("notapokemon"));

        mockMvc.perform(get("/pokemon/notapokemon"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Pokemon not found: \"notapokemon\""));
    }

    @Test
    void returnsBadRequestForInvalidName() throws Exception {
        mockMvc.perform(get("/pokemon/<script>"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsBadRequestForOverlyLongName() throws Exception {
        String longName = "a".repeat(51);
        mockMvc.perform(get("/pokemon/" + longName))
                .andExpect(status().isBadRequest());
    }
}
