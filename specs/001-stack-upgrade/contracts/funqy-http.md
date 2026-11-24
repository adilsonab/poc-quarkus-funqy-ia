# Existing Funqy HTTP Contract

This migration must preserve the observable contract used by the Angular
client. Funqy HTTP is a preview feature in this POC and is intentionally not
replaced with REST.

## List tasks

- **Method and path**: `GET /listTasks`
- **Success payload**: JSON array of task objects, ordered by `id`.
- **Task fields**: `id` (number) and `title` (string).
- **CORS origins**: `http://localhost:4200` and
  `http://127.0.0.1:4200`.

## Create task

- **Method and path**: `POST /createTask`
- **Content type**: `application/json`
- **Request**:

  ```json
  {"title":"Learn Funqy"}
  ```

- **Success payload**: JSON task object with generated `id` and normalized
  `title`.
- **Validation**: input must exist; title must not be null/blank; surrounding
  whitespace is removed; title length must not exceed 255 characters.
- **Persistence failure**: surfaced as a failure, never replaced with a
  success-shaped response.

## Migration checks

`TaskServiceTest` exercises create/list persistence and both HTTP functions,
including the allowed Angular CORS origin. Keep route names, JSON fields,
ordering, and validation behavior unchanged. The test does not currently
specify a precise HTTP status/body for invalid input or database errors; do not
introduce such a promise in migration documentation.
