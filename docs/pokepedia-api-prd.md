# Pokedex PRD

| Changelog        | Version    |
|------------------|------------|
| Initial Analysis | 07/03/2026 |

## Knowledge Base

- https://github.com/PokeAPI/pokeapi
- https://github.com/PokeAPI/pokeapi/blob/master/openapi.yml
- https://raw.githubusercontent.com/PokeAPI/pokeapi/refs/heads/master/openapi.yml

## Tech Stack

- Java 21, with sdkman
- Spring Boot, starter from https://start.spring.io/

---

## Assignment

REST API Pokedex that returns Pokemon information, backed by existing public APIs.

### UC 1 - Basic Pokemon Information

`GET /pokemon/{name}`

Given a Pokemon name, return standard description and additional information.
Calls the [PokeAPI](https://pokeapi.co/) (pokemon-species endpoint).

**Response fields:**
- `name` — Pokemon’s name
- `description` — standard description (from `flavor_text` array, any English entry)
- `habitat` — Pokemon’s habitat
- `isLegendary` — legendary status

**Example:**
```
http http://localhost:5000/pokemon/mewtwo
```
```json
{
  "name": "mewtwo",
  "description": "It was created by a scientist after years of horrific gene splicing and DNA engineering experiments.",
  "habitat": "rare",
  "isLegendary": true
}
```

### UC 2 - Translated Pokemon Description

`GET /pokemon/translated/{name}`

Same response shape as UC 1, but with a translated description. Translation rules:

1. Habitat is `cave` OR legendary → **Yoda** translation
2. All other Pokemon → **Shakespeare** translation
3. Translation fails → fall back to standard description

Calls [PokeAPI](https://pokeapi.co/) + [FunTranslations](https://funtranslations.com).

**Example:**
```
http http://localhost:5000/pokemon/translated/mewtwo
```
```json
{
  "name": "mewtwo",
  "description": "Created by a scientist after years of horrific gene splicing and dna engineering experiments, it was.",
  "habitat": "rare",
  "isLegendary": true
}
```

### External APIs

| API                       | URL                                         |
|---------------------------|---------------------------------------------|
| PokeAPI (pokemon-species) | https://pokeapi.co/                         |
| Shakespeare translator    | https://funtranslations.com/api/shakespeare |
| Yoda translator           | https://funtranslations.com/api/yoda        |

---

## Guidelines

- Concise, readable, correct
- Document production-level design decisions that differ from this implementation
- High-value unit tests
- Focus on design decisions, code layout, and approach

## README Requirements

- How to run (assume nothing installed)
- What you’d do differently for production

## Bonus

- Dockerfile
- Git history