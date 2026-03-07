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

### PokeAPI HTTP Client & Models

Infrastructure layer providing HTTP access to PokeAPI's `pokemon-species` endpoint.
Models generated from a trimmed OpenAPI spec via `openapi-generator-maven-plugin`.
Responses cached with Caffeine (max 500 entries, 1h TTL) to comply with PokeAPI fair-use policy.

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

### UC 2 - FunTranslation API Integration

Infrastructure layer providing HTTP access to the FunTranslations API.
Used by UC 3 to translate Pokemon descriptions into Shakespeare or Yoda style.

**Endpoints:**

| Translation   | URL                                                        |
|---------------|------------------------------------------------------------|
| Shakespeare   | `GET https://api.funtranslations.com/translate/shakespeare.json?text={text}` |
| Yoda          | `GET https://api.funtranslations.com/translate/yoda.json?text={text}`        |

**Response shape** (only `contents.translated` is consumed):
```json
{
  "success": { "total": 1 },
  "contents": {
    "translated": "Translated text here",
    "text": "Original text here",
    "translation": "shakespeare"
  }
}
```

**Rate limits (free tier, no auth required):**
- 5 requests/hour, 60 requests/day
- HTTP 429 when exceeded

**Design notes:**
- Caching translated descriptions is essential given the strict rate limits
- Any failure (HTTP errors, timeouts, 429 rate limits) must fall back silently to the standard description — no error surfaced to the caller
- Response schema is trivial — no OpenAPI codegen needed; a minimal DTO suffices

### UC 3 - Translated Pokemon Description

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

## Production readiness - Next steps
Currently, the tests are run on the commit hook, but the tests are also directly invoking the API from the third-party provider.
So, either we run a simple smoke test locally, or we create a new strategy for the full integration test against the provider.
For instance, PokeAPI does support a self-hosted deployment.

The project is lacking continuous integration and continuous deployment.

The PokeAPI is now integrated using REST, but I saw that there is a GraphQL version.
To be investigated as it could reduce the overall bandwidth and network usage.

The provider's API does include rate limiting and caching. Assuming that our API will be open to the public, we should review our
throttling strategies. Limitations on the FunTranslations API are very strict.

The original content is localized, we could extend our API with localization as well.

Logging and observability.