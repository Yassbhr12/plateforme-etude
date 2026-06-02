# Exceptions - logique et concepts

Ce document explique la logique et les concepts utilises pour la gestion centralisee des exceptions dans le projet.

## Objectif global

- Normaliser les erreurs renvoyees par l'API (meme format pour tous les endpoints).
- Eviter les `try/catch` repetitifs dans les services et controllers.
- Associer chaque erreur metier a un code et un statut HTTP coherents.

## Vue d'ensemble (flux)

1. Un service detecte une situation anormale (ex: entite introuvable).
2. Le service leve une exception metier (ex: `NotFoundException`).
3. `GlobalExceptionHandler` intercepte l'exception.
4. Le handler construit un `ApiError` standardise.
5. Le client recoit une reponse JSON avec statut HTTP + details.

## Classes principales

### `AppException`

- Classe de base pour les exceptions metier.
- Porte le `HttpStatus` et un `errorCode` (code fonctionnel).
- Heritee par les exceptions concretes du projet.

### `ApiError`

- DTO de reponse d'erreur renvoye au client.
- Champs utilises:
  - `timestamp`: horodatage de l'erreur.
  - `status`: code HTTP numerique.
  - `error`: libelle HTTP (ex: "Bad Request").
  - `message`: message lisible (metier ou technique).
  - `path`: endpoint demande.
  - `code`: code fonctionnel (ex: `NOT_FOUND`).
  - `details`: map des erreurs de validation (champ -> message).

### `GlobalExceptionHandler`

- `@RestControllerAdvice` centralise la conversion exception -> reponse HTTP.
- Handlers exposes:
  - `AppException` -> code/statut definis par l'exception.
  - `MethodArgumentNotValidException` -> `VALIDATION_ERROR` + details champ par champ.
  - `HttpMessageNotReadableException` -> `MALFORMED_REQUEST` (JSON invalide).
  - `AccessDeniedException` -> `ACCESS_DENIED`.
  - `Exception` (catch-all) -> `INTERNAL_ERROR`.

## Exceptions metier concretes

Toutes heritent de `AppException` et fixent un statut + un `errorCode` coherent.

| Classe | Statut HTTP | errorCode | Usage typique |
|---|---|---|---|
| `BadRequestException` | 400 | `BAD_REQUEST` | Donnees invalides cote metier.
| `UnauthorizedException` | 401 | `UNAUTHORIZED` | Authentification absente/invalid.
| `ForbiddenException` | 403 | `FORBIDDEN` | Acces refuse (droits).
| `NotFoundException` | 404 | `NOT_FOUND` | Ressource introuvable.
| `ConflictException` | 409 | `CONFLICT` | Conflit d'etat (doublon, etc.).
| `TokenException` | 401 | `TOKEN_ERROR` | Probleme de token (JWT, etc.).

## Exemple de reponse d'erreur

```json
{
  "timestamp": "2026-05-12T10:15:30Z",
  "status": 404,
  "error": "Not Found",
  "message": "User not found",
  "path": "/api/users/42",
  "code": "NOT_FOUND",
  "details": null
}
```

## Validation des entrees

Lorsqu'une validation Bean Validation echoue:

- `MethodArgumentNotValidException` est levee par Spring.
- Le handler construit `details` sous forme `champ -> message`.
- Le statut est `400` avec `code = VALIDATION_ERROR`.

Exemple `details`:

```json
{
  "email": "must be a well-formed email address",
  "password": "size must be between 8 and 50"
}
```

## Bonnes pratiques d'utilisation

- Lever une exception metier precise au plus tot dans le service.
- Utiliser un message clair et stable (eviter les messages techniques).
- Ne pas catcher une exception pour la relancer sans valeur ajoutee.
- Laisser `GlobalExceptionHandler` generer le format de reponse.

## Emplacement des classes

- Package: `com.sge.platforme_etude.helper.exceptions`
- Fichiers: `AppException`, `ApiError`, `GlobalExceptionHandler`, et exceptions concretes.

