# Pokepedia

A REST API Pokedex built with Spring Boot. Wraps [PokeAPI](https://pokeapi.co/) to serve Pokemon info with optional fun translations.

## Endpoints

### Get Pokemon info

```
GET /pokemon/{name}
```

```bash
curl http://localhost:5000/pokemon/pikachu
```

```json
{
  "name": "pikachu",
  "description": "When several of these Pokemon gather, their electricity can build and cause lightning storms.",
  "habitat": "forest",
  "isLegendary": false
}
```

### Get Pokemon info with translated description

```
GET /pokemon/translated/{name}
```

Uses Yoda translation for cave-dwelling or legendary Pokemon, Shakespeare for all others. Falls back to the standard description if the translation service is unavailable.

```bash
curl http://localhost:5000/pokemon/translated/mewtwo
```

## Getting Started

### Java (native)

Prerequisites: Java 21 (install via [sdkman](https://sdkman.io/))

```bash
./mvnw spring-boot:run
```

The API starts on port **5000**.

### Docker

```bash
docker build -t pokepedia .
docker run -p 5000:5000 pokepedia
```

### Docker Compose

```bash
docker compose up --build
```

To override the PokeAPI base URL:

```bash
POKEAPI_BASE_URL=http://localhost:8080 docker compose up --build
```

## Running Tests

```bash
./mvnw test
```

## Production readiness

Documented in [Pokepedia-api-prd - Production Readiness Next-steps](https://github.com/TTia/pokepedia/blob/main/docs/pokepedia-api-prd.md#production-readiness---next-steps)
