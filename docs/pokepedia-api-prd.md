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

I would like to categorise the improvements as mentioned in https://leaddev.com/software-quality/four-pillars-code-health.

### Pillar 1 / Automation — increase delivery lifecycle
CI/CD pipeline. The project currently has no continuous integration or deployment. Set up a pipeline (e.g., GitHub Actions) that runs on every push with at least two stages: a fast gate (compilation, unit tests with mocked HTTP clients) and a slow gate (integration smoke tests against either a self-hosted PokeAPI instance or a contract-test double).

Decouple commit hooks from live APIs. Tests on the pre-commit hook currently invoke third-party providers directly. Move those to the CI slow gate and restrict the hook to offline checks only (mvn install + unit tests with API stubs).
PokemonControllerTest are not hermetic as they actually test against the PokeAPI, not fully self-contained. PokemonControllerIntegrationTest mixes stubbed tests with real scenarios.

Deploy via CD using the Dockerfile.

### Pillar 2 / Insights — observability and control
Structured logging: apply log lines with structured logging (i.e., APM with MDC Logging using Json). Include correlation IDs across the chained PokeAPI -> FunTranslations call sequence so a single request can be traced end to end.

Health and readiness endpoints. Expose Spring Boot Actuator /actuator/health (with downstream checks for third parties' API for reachability) and /actuator/info (build version, commit SHA).

### Pillar 3 / Maintainability — refactoring, testing, documentation
Prioritisation of the test suite: unit tests with stubs, integration tests, and contract tests against PokeAPI and FunTranslations APIs to avoid response-shape drift. High-value tests should cover the translation selection logic (cave/legendary -> Yoda, else Shakespeare) - which is the "core" business logic at the moment.

Publish the Pokepedia API's own OpenAPI spec. Version it from day one (/v1/pokemon/...) to better handle breaking changes.

PokeAPI offers a GraphQL endpoint. Evaluate switching to it for the species query — it could reduce payload size significantly since only name, flavor_text_entries, habitat and is_legendary are consumed today.

Extract a reusable pattern for calling rate-limited third-party APIs (backoff, circuit-breaking, fallback). This avoids re-inventing the strategy when future integrations are added.

Instead of invoking a public API, we could deploy our own instance of PokeApi (various deployment options available: from static-data or Django BE self-hosted). Replace FunTranslations API or question the UC (brainstorming here: https://pokeapi.co/api/v2/pokemon-species/?limit=0 returns 1025 unique pokemon, for the sake of the exercise, we could extend the static json data with one-time generated "fun translations").

The PokeAPI flavour text is already localised. Extend the Pokedex API with an Accept-Language header to serve descriptions in the caller's language, falling back to English.

### Pillar 4 / Security
Input validation. Already we are sanitizing and validating the input, but requesting data against PokeApi is a "costly" operation. Assuming that the Pokemon population changes rarely and we really want to use the public PokeAPI, we could generate a bloom filter (brainstoming here) to avoid pulling data from PokeApi with a reasonable degree of confidence to hit an existing Pokemon.

Apply rate limits on the public-facing endpoints to protect both the service and the upstream providers from abuse.

Require TLS termination at the load balancer or reverse proxy.
