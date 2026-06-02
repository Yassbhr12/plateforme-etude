# Documentation des changements backend

Ce document explique les fonctionnalites ajoutees dans le backend, leur role, leur fonctionnement et les raisons techniques/securite derriere chaque choix.

## 1. Reinitialisation du mot de passe par code email

### Objectif

Permettre a un utilisateur qui a oublie son mot de passe de definir un nouveau mot de passe sans etre connecte.

La solution ajoutee utilise un code envoye par email. Ce code est temporaire, a usage unique, limite en tentatives, et il n'est jamais stocke en clair dans la base de donnees.

### Fichiers ajoutes

- `src/main/java/com/sge/platforme_etude/entite/PasswordResetToken.java`
- `src/main/java/com/sge/platforme_etude/repository/PasswordResetTokenRepo.java`
- `src/main/java/com/sge/platforme_etude/dto/authentification/ForgotPasswordRequest.java`
- `src/main/java/com/sge/platforme_etude/dto/authentification/ResetPasswordRequest.java`

### Fichiers modifies

- `src/main/java/com/sge/platforme_etude/controller/AuthRequestController.java`
- `src/main/java/com/sge/platforme_etude/service/user/UserService.java`
- `src/main/java/com/sge/platforme_etude/helper/security/config/SecurityConfig.java`
- `src/main/java/com/sge/platforme_etude/repository/UserRepo.java`

## 2. Entite `PasswordResetToken`

### Role

L'entite `PasswordResetToken` represente une demande de reinitialisation de mot de passe.

Elle permet de stocker :

- l'utilisateur concerne,
- le hash du code envoye par email,
- la date de creation,
- la date d'expiration,
- la date d'utilisation,
- le nombre de tentatives deja effectuees.

### Pourquoi une entite separee ?

Une entite separee evite de melanger les codes de connexion/2FA avec les codes de reinitialisation du mot de passe.

C'est plus propre parce que les deux usages n'ont pas le meme role :

- le code de connexion sert a valider une authentification,
- le code de reset sert a autoriser un changement de mot de passe.

Cela permet aussi de garder un historique minimal des demandes de reset et d'invalider correctement les anciens codes.

### Table creee

Avec `spring.jpa.hibernate.ddl-auto=update`, Hibernate cree automatiquement la table :

```sql
password_reset_tokens
```

Champs principaux :

```text
id
user_id
code_hash
created_at
expires_at
used_at
attempts
```

### Securite importante

Le code envoye par email n'est pas stocke en clair. Le backend stocke uniquement `codeHash`.

Concretement :

- l'utilisateur recoit un code comme `123456`,
- le backend stocke `bcrypt(123456)`,
- au moment de la validation, le backend compare le code saisi avec le hash via `encoder.matches(...)`.

Ainsi, si la base de donnees est compromise, les codes actifs ne sont pas lisibles directement.

## 3. Repository `PasswordResetTokenRepo`

### Role

Le repository permet de manipuler les tokens de reinitialisation.

Methodes ajoutees :

```java
Optional<PasswordResetToken> findFirstByUserAndUsedAtIsNullOrderByCreatedAtDesc(User user);
List<PasswordResetToken> findAllByUserAndUsedAtIsNull(User user);
```

### Pourquoi ces methodes ?

`findFirstByUserAndUsedAtIsNullOrderByCreatedAtDesc`

Permet de recuperer le dernier code non utilise pour un utilisateur. C'est celui qui doit etre valide lorsque l'utilisateur saisit son code.

`findAllByUserAndUsedAtIsNull`

Permet d'invalider tous les anciens codes encore actifs avant d'en creer un nouveau. Cela evite qu'un ancien code reste utilisable apres une nouvelle demande.

## 4. DTO `ForgotPasswordRequest`

### Role

Ce DTO represente la premiere etape du reset password : demander l'envoi d'un code.

Payload attendu :

```json
{
  "email": "user@email.com"
}
```

### Validations

Le champ `email` est valide avec :

```java
@NotBlank
@Email
@Size(max = 150)
```

### Pourquoi ?

Ces validations empechent :

- une adresse vide,
- une valeur qui n'est pas un email,
- une chaine trop longue.

Cela protege l'API contre les entrees invalides et garde une validation coherente avec les autres DTO d'authentification.

## 5. DTO `ResetPasswordRequest`

### Role

Ce DTO represente la deuxieme etape : valider le code et definir un nouveau mot de passe.

Payload attendu :

```json
{
  "email": "user@email.com",
  "code": "123456",
  "newPassword": "nouveauMotDePasse123"
}
```

### Validations

```java
@Email
@Pattern(regexp = "\\d{6}")
@Size(min = 8, max = 255)
```

### Pourquoi ?

- `email` identifie le compte concerne.
- `code` doit contenir exactement 6 chiffres.
- `newPassword` doit respecter une longueur minimale de 8 caracteres.

Le mot de passe n'est jamais sauvegarde en clair : il est hash avec bcrypt avant d'etre stocke.

## 6. Endpoint `POST /api/auth/password/forgot`

### Role

Demander l'envoi d'un code de reinitialisation par email.

### Controller

Ajoute dans `AuthRequestController` :

```java
@PostMapping("/password/forgot")
public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request)
```

### Requete

```http
POST /api/auth/password/forgot
Content-Type: application/json
```

```json
{
  "email": "user@email.com"
}
```

### Reponse

```json
{
  "message": "Si cette adresse existe, un code de reinitialisation a ete envoye."
}
```

### Pourquoi une reponse generique ?

La reponse ne dit jamais si l'email existe ou non.

C'est volontaire et important pour la securite. Sinon, une personne pourrait tester beaucoup d'emails et savoir lesquels existent dans l'application.

Ce risque s'appelle l'enumeration d'utilisateurs.

### Fonctionnement interne

Dans `UserService.requestPasswordReset(...)` :

1. Le backend normalise l'email.
2. Il cherche l'utilisateur sans tenir compte de la casse.
3. Si l'utilisateur n'existe pas, il ne fait rien et renvoie quand meme une reponse generique.
4. Si le compte est desactive, il ne fait rien.
5. Il verifie s'il y a deja un code recent.
6. Il invalide les anciens codes actifs.
7. Il genere un nouveau code a 6 chiffres.
8. Il stocke uniquement le hash du code.
9. Il envoie le code par email.

## 7. Endpoint `POST /api/auth/password/reset`

### Role

Valider le code recu par email et changer le mot de passe.

### Controller

Ajoute dans `AuthRequestController` :

```java
@PostMapping("/password/reset")
public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request)
```

### Requete

```http
POST /api/auth/password/reset
Content-Type: application/json
```

```json
{
  "email": "user@email.com",
  "code": "123456",
  "newPassword": "nouveauMotDePasse123"
}
```

### Reponse succes

```json
{
  "message": "Mot de passe mis a jour avec succes."
}
```

### Fonctionnement interne

Dans `UserService.resetForgottenPassword(...)` :

1. Le backend cherche l'utilisateur par email.
2. Il recupere le dernier code non utilise.
3. Il verifie que le code n'est pas expire.
4. Il verifie que le nombre de tentatives n'est pas depasse.
5. Il compare le code saisi avec le hash stocke.
6. Il verifie que le nouveau mot de passe est different de l'ancien.
7. Il hash le nouveau mot de passe avec bcrypt.
8. Il marque le token comme utilise avec `usedAt`.
9. Il sauvegarde le nouveau mot de passe.
10. Il revoque les refresh tokens existants de l'utilisateur.

### Pourquoi revoquer les refresh tokens ?

Si quelqu'un avait deja un refresh token actif pour ce compte, il pourrait continuer a obtenir de nouveaux access tokens meme apres le changement du mot de passe.

La revocation force donc une reconnexion propre apres le reset password.

## 8. Regles de securite du reset password

### Expiration du code

```java
PASSWORD_RESET_EXPIRATION = Duration.ofMinutes(10)
```

Le code expire apres 10 minutes.

Pourquoi : reduire la fenetre d'attaque si quelqu'un accede tardivement a l'email ou intercepte le code.

### Cooldown de renvoi

```java
PASSWORD_RESET_RESEND_COOLDOWN = Duration.ofSeconds(60)
```

Le backend evite de generer trop rapidement plusieurs codes.

Pourquoi :

- limiter le spam email,
- eviter les abus,
- eviter de remplacer le code trop vite pendant que l'utilisateur lit son email.

### Limite de tentatives

```java
PASSWORD_RESET_MAX_ATTEMPTS = 5
```

Apres 5 tentatives incorrectes, le code est invalide.

Pourquoi : limiter les attaques par brute force sur le code a 6 chiffres.

### Code a usage unique

Le champ `usedAt` indique qu'un code a deja ete consomme.

Une fois utilise, le code ne peut plus etre reutilise.

### Hash du code

Le code est hash avec bcrypt, comme les mots de passe.

Pourquoi : ne pas exposer les codes actifs en cas de fuite de base de donnees.

### Nouveau mot de passe different de l'ancien

Le backend refuse un nouveau mot de passe identique a l'ancien.

Pourquoi : s'assurer que l'operation change reellement le secret du compte.

## 9. Configuration Spring Security

### Modification

Dans `SecurityConfig`, ces routes ont ete ajoutees en `permitAll` :

```java
.requestMatchers("/api/auth/password/forgot").permitAll()
.requestMatchers("/api/auth/password/reset").permitAll()
```

### Pourquoi ?

Ces endpoints doivent etre accessibles sans token JWT, car l'utilisateur qui a oublie son mot de passe n'est pas connecte.

Les autres routes sensibles restent protegees par JWT ou par role admin.

## 10. Recherche email insensible a la casse

### Modification

Dans `UserRepo`, ajout de :

```java
Optional<User> findByEmailIgnoreCase(String email);
```

### Role

Permet de retrouver un utilisateur meme si l'email est saisi avec une casse differente.

Exemple :

```text
User@Test.com
user@test.com
USER@test.com
```

Ces variantes pointent vers le meme compte.

### Pourquoi ?

Les emails sont generalement traites comme insensibles a la casse dans les applications modernes.

Cela evite des echecs de connexion ou de reset password simplement parce que l'utilisateur a saisi une majuscule.

## 11. Normalisation email

### Modification

Dans `UserService`, ajout d'une methode :

```java
private String normalizeEmail(String email)
```

### Role

Cette methode :

- supprime les espaces autour de l'email,
- convertit l'email en minuscules.

### Utilisation

Elle est utilisee lors :

- de l'inscription,
- de la connexion,
- de la validation du code de connexion,
- de la demande de reset password,
- du changement de mot de passe,
- de la recherche utilisateur par email.

### Pourquoi ?

Cela evite les doublons et les incoherences :

```text
" user@email.com "
"USER@email.com"
"user@email.com"
```

Ces valeurs sont normalisees en :

```text
user@email.com
```

## 12. Generation securisee des codes

### Modification

Le generateur de code utilise maintenant :

```java
SecureRandom
```

au lieu de :

```java
Random
```

### Pourquoi ?

`Random` est suffisant pour des usages simples, mais pas ideal pour des codes de securite.

`SecureRandom` est plus adapte pour generer des valeurs sensibles comme :

- code de validation,
- code de reinitialisation,
- token temporaire.

## 13. Envoi email

### Service utilise

Le reset password reutilise le service existant :

```java
EmailService.sendEmail(...)
```

### Sujet de l'email

```text
Reinitialisation du mot de passe
```

### Contenu

L'email contient :

- le code de reinitialisation,
- la duree d'expiration,
- un message indiquant d'ignorer l'email si l'utilisateur n'a rien demande.

### Configuration necessaire

Dans `application.properties`, l'envoi email depend de :

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${SPRING_MAIL_USERNAME:...}
spring.mail.password=${SPRING_MAIL_PASSWORD:}
```

En Docker, il faut fournir ces variables dans `.env` :

```env
SPRING_MAIL_USERNAME=...
SPRING_MAIL_PASSWORD=...
```

Pour Gmail, il faut utiliser un mot de passe d'application, pas le mot de passe normal du compte Gmail.

## 14. Fonctionnalites admin ajoutees dans le backend

En plus du reset password, des fonctionnalites admin ont ete ajoutees pour mieux gerer les utilisateurs.

### 14.1 Activation et desactivation d'un utilisateur

Endpoint ajoute :

```http
PUT /api/admin/users/{id}/toggle-status
```

### Role

Permet a un administrateur d'activer ou desactiver un utilisateur.

### Pourquoi ?

Un admin doit pouvoir bloquer temporairement un compte sans le supprimer definitivement.

### Regles de securite

Le backend empeche :

- un admin de desactiver son propre compte,
- la desactivation du dernier administrateur actif.

Ces protections evitent de bloquer totalement l'administration de l'application.

### 14.2 Changement de role utilisateur

Endpoint ajoute :

```http
PUT /api/admin/users/{id}/role
```

Payload :

```json
{
  "role": "ROLE_ADMIN"
}
```

ou :

```json
{
  "role": "ROLE_USER"
}
```

### Role

Permet a un administrateur de promouvoir un utilisateur en admin ou de retirer le role admin.

### Pourquoi ?

La gestion des roles fait partie des fonctionnalites essentielles d'un espace administrateur.

### Regles de securite

Le backend empeche :

- un admin de retirer son propre role admin,
- le retrait du role admin du dernier administrateur actif.

### 14.3 Suppression utilisateur protegee

La methode de suppression utilisateur a ete renforcee :

```java
deleteUserById(Long id, Long currentUserId)
```

### Role

Supprimer un utilisateur tout en tenant compte de l'admin actuellement connecte.

### Regles de securite

Le backend empeche :

- un administrateur de supprimer son propre compte,
- la suppression du dernier administrateur actif.

### Pourquoi ?

Sans cette protection, un admin pourrait supprimer le dernier compte capable d'administrer la plateforme.

## 15. Comptage des administrateurs actifs

### Modification

Dans `UserRepo`, ajout de :

```java
long countByRoleAndActifTrue(Role role);
```

### Role

Compter combien d'utilisateurs actifs ont un role donne, notamment `ROLE_ADMIN`.

### Utilisation

Cette methode est utilisee pour empecher les actions dangereuses sur le dernier admin actif :

- suppression,
- desactivation,
- changement de role.

## 16. Inscription publique forcee en `ROLE_USER`

### Modification

Lors de l'inscription publique, le backend force maintenant :

```java
user.setRole(Role.ROLE_USER);
```

### Role

Garantir qu'un utilisateur cree via `/api/auth/register` ne peut pas choisir lui-meme le role admin.

### Pourquoi ?

Sans cette protection, un utilisateur malveillant pourrait envoyer dans le body :

```json
{
  "role": "ROLE_ADMIN"
}
```

et creer un compte administrateur si le backend accepte ce champ.

Maintenant, seul un admin deja authentifie peut gerer les roles via les endpoints admin.

## 17. Resume des endpoints backend ajoutes

### Auth publique

```http
POST /api/auth/password/forgot
POST /api/auth/password/reset
```

### Admin

```http
PUT /api/admin/users/{id}/toggle-status
PUT /api/admin/users/{id}/role
```

## 18. Resume des garanties de securite

Les changements ajoutent les garanties suivantes :

- code de reset non stocke en clair,
- expiration automatique des codes,
- usage unique des codes,
- limite de tentatives,
- cooldown de renvoi,
- reponse generique pour eviter l'enumeration d'emails,
- revoke des refresh tokens apres reset password,
- inscription publique toujours en `ROLE_USER`,
- protection contre la suppression/desactivation du dernier admin actif,
- protection contre l'auto-suppression admin,
- protection contre l'auto-desactivation admin,
- recherche email insensible a la casse,
- generation des codes avec `SecureRandom`.

## 19. Commandes de verification

Compilation backend :

```bash
mvn.cmd -q -DskipTests compile
```

Lancement avec Docker :

```bash
docker compose -f docker-compose.prod.yml up -d --force-recreate --build
```

Tester la demande de reset password :

```bash
curl -X POST http://localhost/api/auth/password/forgot \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"user@email.com\"}"
```

Tester le changement du mot de passe :

```bash
curl -X POST http://localhost/api/auth/password/reset \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"user@email.com\",\"code\":\"123456\",\"newPassword\":\"nouveauMotDePasse123\"}"
```

En developpement local backend direct, remplacer `http://localhost/api` par `http://localhost:8080/api`.

