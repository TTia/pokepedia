package com.ttia.pokepedia.funtranslations;

public enum TranslationType {
    SHAKESPEARE("shakespeare"),
    YODA("yoda");

    private final String pathSegment;

    TranslationType(String pathSegment) {
        this.pathSegment = pathSegment;
    }

    public String getPathSegment() {
        return pathSegment;
    }
}
