package com.ttia.pokepedia.funtranslations.model;

public record TranslationResponse(Contents contents) {

    public record Contents(String translated) {
    }
}
