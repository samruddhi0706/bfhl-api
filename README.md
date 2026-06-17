# BFHL REST API — D.Y.Patil Campus Hiring (June 2026)

## Tech Stack
- Java 17
- Spring Boot 3.2.5
- Maven
- JUnit 5 + Mockito (unit tests)
- Spring MockMvc (integration tests)
- JaCoCo (≥80% service-layer coverage enforced)

---

## Architecture (Layered)

```
Controller  →  Service Interface  →  Service Implementation
                                         ↓
                                       DTOs
                                         ↓
                               GlobalExceptionHandler
```

---

## Running Locally

```bash
# Build
mvn clean package

# Run
java -jar target/bfhl-api-1.0.0.jar

# Test all
mvn test
```

---

## API Reference

### `POST /bfhl`

**Headers**
| Header | Required | Description |
|---|---|---|
| `X-Request-Id` | No | Echoed back in response header and body |
| `Content-Type` | Yes | `application/json` |

**Request Body**
```json
{
  "data": ["A", "1", "22", "$", "B", "7"]
}
```

**Response**
```json
{
  "is_success": true,
  "request_id": "REQ-1001",
  "odd_numbers": ["1", "7"],
  "even_numbers": ["22"],
  "alphabets": ["A", "B"],
  "special_characters": ["$"],
  "sum": "30",
  "largest_number": "22",
  "smallest_number": "1",
  "alphabet_count": 2,
  "number_count": 3,
  "special_character_count": 1,
  "contains_duplicates": false,
  "unique_element_count": 6,
  "sorted_numbers": ["1", "7", "22"],
  "vowel_count": 0,
  "consonant_count": 2,
  "alphabet_frequency": { "A": 1, "B": 1 },
  "longest_alphabetic_value": "A",
  "shortest_alphabetic_value": "A",
  "processing_time_ms": 15,
  "summary": {
    "total_elements_received": 6,
    "valid_elements_processed": 6,
    "invalid_elements_ignored": 0
  }
}
```

---

## Processing Rules

| Input | Treatment |
|---|---|
| Pure number (`"1"`, `"-10"`, `"25.5"`) | Numeric — contributes to sum, odd/even, sorted_numbers |
| Pure alpha (`"A"`, `"ABC"`) | Alphabet — contributes to alphabet_count, vowel_count, frequency |
| Single special char (`"$"`, `"#"`) | Special — contributes to special_character_count |
| Alphanumeric (`"A1B2"`) | Split: digits → numeric, letters → alphabetic |
| `null`, `""`, `"   "` | Ignored — counted in `invalid_elements_ignored` |
| Decimal numbers (`"25.5"`) | Included in sum/largest/smallest; excluded from odd/even |
| Duplicate values | De-duplicated; `contains_duplicates` set to `true` |

---

## Deploying to Render

1. Push to GitHub
2. Create a new **Web Service** on [render.com](https://render.com)
3. Connect your repo — Render auto-detects the `render.yaml`
4. Set **Build Command**: `mvn clean package -DskipTests`
5. Set **Start Command**: `java -jar target/bfhl-api-1.0.0.jar`

---

## Deploying to Railway

```bash
railway login
railway init
railway up
```

Set environment variable `PORT=8080`.

---

## Test Coverage

Run `mvn test` — JaCoCo enforces **≥80% line coverage** on the `service` package.
Coverage report generated at `target/site/jacoco/index.html`.
