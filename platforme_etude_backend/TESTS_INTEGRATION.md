# Documentation Complète — Tests d'Intégration PostgreSQL

## Platforme Étude Backend

---

## 1. Introduction

### 1.1 Qu'est-ce qu'un test d'intégration ?

Un **test d'intégration** vérifie que plusieurs composants de l'application fonctionnent correctement ensemble. Contrairement aux tests unitaires qui isolent chaque classe, les tests d'intégration testent l'interaction réelle entre :
- Les **repositories** et la **base de données PostgreSQL**
- Les **services** et leurs dépendances (repositories, mappers)
- Les **contrôleurs REST** et le pipeline HTTP complet (sérialisation, validation, sécurité)

### 1.2 Pourquoi PostgreSQL et pas H2 ?

| Critère | H2 (in-memory) | PostgreSQL (Testcontainers) |
|---|---|---|
| Fidélité au comportement production | ❌ Différences de dialecte SQL | ✅ Identique à la production |
| Contraintes et types de données | ⚠️ Comportement différent parfois | ✅ Comportement identique |
| Fonctions SQL spécifiques | ❌ Non supportées | ✅ Supportées nativement |
| Vitesse d'exécution | ✅ Très rapide | ⚠️ Plus lent (démarrage conteneur) |
| Fiabilité des résultats | ⚠️ Faux positifs possibles | ✅ Résultats fiables |

### 1.3 Technologie utilisée : Testcontainers

**Testcontainers** est une bibliothèque Java qui permet de lancer des conteneurs Docker légers pendant les tests. Pour notre projet, un conteneur **PostgreSQL 16 Alpine** est démarré automatiquement avant les tests et arrêté après.

---

## 2. Architecture des Tests

### 2.1 Diagramme de structure

```
src/test/
├── resources/
│   └── application-test.properties          # Configuration du profil "test"
└── java/com/sge/platforme_etude/
    ├── AbstractIntegrationTest.java          # Classe de base (PostgreSQL container)
    ├── TestDataFactory.java                  # Factory de données de test
    ├── repository/                           # Tests des 11 repositories
    │   ├── UserRepoIntegrationTest.java
    │   ├── MatiereRepoIntegrationTest.java
    │   ├── GroupeEtudeRepoIntegrationTest.java
    │   ├── SessionEtudeRepoIntegrationTest.java
    │   ├── ObjectifHebdoRepoIntegrationTest.java
    │   ├── InvitationRepoIntegrationTest.java
    │   ├── NotificationRepoIntegrationTest.java
    │   ├── CommentaireRepoIntegrationTest.java
    │   ├── DisponibiliteRepoIntegrationTest.java
    │   ├── MessageChatRepoIntegrationTest.java
    │   └── RefreshTokenRepoIntegrationTest.java
    ├── service/                              # Tests des 3 services principaux
    │   ├── UserServiceIntegrationTest.java
    │   ├── MatiereServiceIntegrationTest.java
    │   └── GroupeEtudeServiceIntegrationTest.java
    └── controller/                           # Tests du contrôleur REST
        └── AuthControllerIntegrationTest.java
```

### 2.2 Hiérarchie d'héritage

```
AbstractIntegrationTest (classe abstraite)
    │
    ├── @Testcontainers         → Active le cycle de vie des conteneurs
    ├── @SpringBootTest         → Charge le contexte Spring complet
    ├── @ActiveProfiles("test") → Utilise application-test.properties
    │
    ├──── UserRepoIntegrationTest
    ├──── MatiereRepoIntegrationTest
    ├──── GroupeEtudeRepoIntegrationTest
    ├──── SessionEtudeRepoIntegrationTest
    ├──── ObjectifHebdoRepoIntegrationTest
    ├──── InvitationRepoIntegrationTest
    ├──── NotificationRepoIntegrationTest
    ├──── CommentaireRepoIntegrationTest
    ├──── DisponibiliteRepoIntegrationTest
    ├──── MessageChatRepoIntegrationTest
    ├──── RefreshTokenRepoIntegrationTest
    ├──── UserServiceIntegrationTest
    ├──── MatiereServiceIntegrationTest
    ├──── GroupeEtudeServiceIntegrationTest
    └──── AuthControllerIntegrationTest
```

---

## 3. Configuration

### 3.1 Dépendances Maven (pom.xml)

Trois dépendances Testcontainers ont été ajoutées au `pom.xml` :

```xml
<!-- Testcontainers core -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <scope>test</scope>
</dependency>

<!-- Intégration JUnit 5 -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- Module PostgreSQL -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

### 3.2 Fichier application-test.properties

Ce fichier configure le profil `test` utilisé exclusivement pendant les tests :

```properties
# Hibernate crée et détruit le schéma automatiquement
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT de test
jwt.secret=TestSecretKeyForIntegrationTests123456789...
jwt.expiration=900000
jwt.refresh-expiration=604800000

# Email désactivé
spring.mail.host=localhost
spring.mail.port=25
```

**Points clés :**
- `ddl-auto=create-drop` : Le schéma est recréé à chaque lancement de test
- Les propriétés `datasource` (URL, username, password) sont **injectées dynamiquement** par Testcontainers

### 3.3 Classe de base : AbstractIntegrationTest

```java
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("platforme_etude_test_db")
                    .withUsername("test_user")
                    .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name",
                      () -> "org.postgresql.Driver");
    }
}
```

**Fonctionnement :**
1. `@Container` démarre un conteneur PostgreSQL 16 Alpine avant tous les tests
2. `@DynamicPropertySource` injecte l'URL JDBC, le username et le password générés dynamiquement
3. Le conteneur est partagé entre toutes les classes de test (static)
4. Le conteneur est automatiquement arrêté après tous les tests

---

## 4. TestDataFactory — Factory de données de test

Classe utilitaire centralisée qui fournit des méthodes statiques pour créer chaque type d'entité avec des données valides par défaut.

| Méthode | Entité créée | Paramètres principaux |
|---|---|---|
| `createUser(nom, prenom, email)` | `User` avec ROLE_USER | Nom, prénom, email |
| `createAdmin(nom, prenom, email)` | `User` avec ROLE_ADMIN | Nom, prénom, email |
| `createMatiere(nom, priorite, user)` | `Matiere` | Nom, priorité (0-5), utilisateur |
| `createGroupeEtude(nom, desc, admin)` | `GroupeEtude` | Nom, description, admin |
| `createSessionEtude(titre, user, matiere, statut)` | `SessionEtude` | Titre, user, matière, statut |
| `createCommentaire(contenu, user, session)` | `Commentaire` | Contenu, user, session |
| `createDisponibilite(jour, debut, fin, user)` | `Disponibilite` | Jour (1-7), heures, user |
| `createObjectifHebdo(semaine, heures, user, matiere)` | `ObjectifHebdo` | Semaine, heures cibles |
| `createNotification(type, message, user)` | `Notification` | Type, message, user |
| `createInvitation(statut, groupe, sender, receiver)` | `Invitation` | Statut, groupe, users |
| `createMessageChat(contenu, user, groupe)` | `MessageChat` | Contenu, user, groupe |
| `createRefreshToken(token, user)` | `RefreshToken` | Token string, user |

---

## 5. Détail de chaque classe de test

### 5.1 Tests de Repository (11 classes)

#### 5.1.1 UserRepoIntegrationTest — 7 tests

| Test | Description | Ce qui est vérifié |
|---|---|---|
| `shouldSaveAndFindUserById` | Sauvegarde un User et le retrouve par ID | Persistance complète (nom, prénom, email, rôle) |
| `shouldFindUserByEmail` | Recherche par email | Méthode `findUserByEmail()` |
| `shouldReturnEmptyWhenEmailNotFound` | Email inexistant | Retourne `Optional.empty()` |
| `shouldFindUsersByNom` | Recherche par nom | Méthode `findUserByNom()` avec 2 résultats |
| `shouldFindUsersByRole` | Recherche par rôle | Filtre ROLE_ADMIN vs ROLE_USER |
| `shouldUpdateUser` | Modification d'un champ | Persistance de la mise à jour |
| `shouldEnforceUniqueEmailConstraint` | Email en double | Exception levée par la contrainte UNIQUE |

#### 5.1.2 MatiereRepoIntegrationTest — 5 tests

| Test | Description |
|---|---|
| `shouldSaveAndFindById` | CRUD basique avec vérification des champs et relation User |
| `shouldFindMatiereByNom` | Recherche par nom de matière |
| `shouldFindMatieresByUserId` | Filtrage par utilisateur (2 matières pour un user, 1 pour un autre) |
| `shouldReturnEmptyWhenNomNotFound` | Nom inexistant retourne `Optional.empty()` |
| `shouldDeleteMatiere` | Suppression et vérification d'absence |

#### 5.1.3 GroupeEtudeRepoIntegrationTest — 6 tests

| Test | Description |
|---|---|
| `shouldSaveAndFindById` | CRUD avec vérification du nom, description et admin |
| `shouldFindGroupeEtudeByNom` | Recherche par nom |
| `shouldCheckExistsByNom` | `existsGroupeEtudeByNom()` — true et false |
| `shouldFindGroupeEtudeByAdmin` | Filtrage par admin (2 groupes pour un admin, 1 pour un autre) |
| `shouldDeleteGroupeEtude` | Suppression |
| `shouldEnforceUniqueNomConstraint` | Contrainte UNIQUE sur le nom du groupe |

#### 5.1.4 SessionEtudeRepoIntegrationTest — 5 tests

| Test | Description |
|---|---|
| `shouldSaveAndFindById` | CRUD avec vérification titre et statut |
| `shouldFindByUserId` | Recherche par utilisateur |
| `shouldFindByMatiereId` | Recherche par matière |
| `shouldFindByStatut` | Filtrage PLANIFIEE vs TERMINEE |
| `shouldFindByDateRange` | Requête complexe : `findByUserIdAndDateDebutGreaterThanEqualAndDateDebutLessThan` |

#### 5.1.5 ObjectifHebdoRepoIntegrationTest — 7 tests

| Test | Description |
|---|---|
| `shouldSaveAndFind` | CRUD basique |
| `shouldFindByUserId` | Recherche par user |
| `shouldFindByMatiereId` | Recherche par matière |
| `shouldFindByUserIdAndMatiereIdAndSemaine` | Recherche composite à 3 critères |
| `shouldCheckExistence` | `existsByUserIdAndMatiereIdAndSemaine()` — true et false |
| `shouldFindBySemaine` | Recherche par semaine |
| `shouldEnforceUniqueConstraint` | Contrainte UNIQUE composée (user + matiere + semaine) |

#### 5.1.6 InvitationRepoIntegrationTest — 6 tests

| Test | Description |
|---|---|
| `shouldSaveAndFind` | CRUD basique |
| `shouldFindBySender` | Recherche par expéditeur |
| `shouldFindByReceiver` | Recherche par destinataire |
| `shouldFindByStatut` | Filtrage EN_ATTENTE vs ACCEPTEE |
| `shouldFindByGroupeEtudeId` | Recherche par groupe |
| `shouldCheckExistsByGroupeAndReceiverAndStatut` | Existence d'invitation en attente |

#### 5.1.7 NotificationRepoIntegrationTest — 4 tests

| Test | Description |
|---|---|
| `shouldSaveAndFind` | CRUD basique |
| `shouldFindByType` | Filtrage par TypeNotif (RAPPEL_SESSION, INVITATION_GROUPE) |
| `shouldFindByUser` | Recherche par objet User |
| `shouldFindByUserId` | Recherche par userId (Long) |

#### 5.1.8 CommentaireRepoIntegrationTest — 3 tests

| Test | Description |
|---|---|
| `shouldSaveAndFind` | CRUD basique |
| `shouldFindByUserId` | Recherche par utilisateur |
| `shouldFindBySessionEtudeId` | Recherche par session d'étude |

#### 5.1.9 DisponibiliteRepoIntegrationTest — 3 tests

| Test | Description |
|---|---|
| `shouldSaveAndFind` | CRUD avec jour de semaine et heures |
| `shouldFindByUserId` | Recherche par utilisateur (2 disponibilités) |
| `shouldDelete` | Suppression et vérification |

#### 5.1.10 MessageChatRepoIntegrationTest — 3 tests

| Test | Description |
|---|---|
| `shouldSaveAndFind` | CRUD basique |
| `shouldFindByGroupeEtudeId` | Recherche par groupe (2 messages) |
| `shouldFindByUserId` | Recherche par utilisateur |

#### 5.1.11 RefreshTokenRepoIntegrationTest — 4 tests

| Test | Description |
|---|---|
| `shouldSaveAndFindByToken` | Recherche par chaîne de token |
| `shouldFindAllByUser` | Tous les tokens d'un utilisateur |
| `shouldDeleteByUser` | Suppression de tous les tokens d'un user |
| `shouldCheckExpiry` | Vérification de la méthode `isExpired()` |

---

### 5.2 Tests de Service (3 classes)

#### 5.2.1 UserServiceIntegrationTest — 7 tests

| Test | Description | Exception attendue |
|---|---|---|
| `shouldCreateUser` | Création via `UserDto` + `AuthRequest` | — |
| `shouldThrowConflictOnDuplicateEmail` | Email déjà utilisé | `ConflictException` |
| `shouldFindById` | Recherche par ID | — |
| `shouldThrowNotFoundById` | ID inexistant (99999) | `NotFoundException` |
| `shouldFindAll` | Liste de tous les utilisateurs | — |
| `shouldFindByEmail` | Recherche par email | — |
| `shouldUpdateUser` | Mise à jour du nom | — |
| `shouldDeleteUser` | Suppression et vérification | `NotFoundException` après suppression |

#### 5.2.2 MatiereServiceIntegrationTest — 8 tests

| Test | Description | Exception attendue |
|---|---|---|
| `shouldCreateMatiere` | Création avec userId valide | — |
| `shouldThrowNotFoundWhenUserNotExists` | userId invalide | `NotFoundException` |
| `shouldFindMatiereById` | Recherche par ID | — |
| `shouldFindAllMatieres` | Liste complète | — |
| `shouldFindMatieresByUserId` | Filtrage par propriétaire | — |
| `shouldUpdateMatiereByOwner` | Mise à jour par le propriétaire | — |
| `shouldForbidUpdateByNonOwner` | Mise à jour par un autre user | `ForbiddenException` |
| `shouldDeleteMatiereByOwner` | Suppression par le propriétaire | — |
| `shouldForbidDeleteByNonOwner` | Suppression par un non-propriétaire | `ForbiddenException` |

#### 5.2.3 GroupeEtudeServiceIntegrationTest — 9 tests

| Test | Description | Exception attendue |
|---|---|---|
| `shouldCreateGroupe` | Création avec adminId | — |
| `shouldCreateGroupeForCurrentUser` | Création pour l'user courant | — |
| `shouldFindById` | Recherche par ID | — |
| `shouldThrowNotFound` | ID inexistant | `NotFoundException` |
| `shouldFindAll` | Liste complète | — |
| `shouldFindByAdminId` | Filtrage par admin | — |
| `shouldUpdateGroupe` | Mise à jour nom et description | — |
| `shouldForbidUpdateByNonAdmin` | Mise à jour par un non-admin | `ForbiddenException` |
| `shouldDeleteGroupe` | Suppression | — |
| `shouldForbidDeleteByNonAdmin` | Suppression par un non-admin | `ForbiddenException` |

---

### 5.3 Tests de Controller (1 classe)

#### 5.3.1 AuthControllerIntegrationTest — 4 tests

Utilise **MockMvc** avec `@AutoConfigureMockMvc` pour tester les endpoints REST.

| Test | Endpoint | Status HTTP | Description |
|---|---|---|---|
| `shouldRegisterUser` | `POST /api/auth/register` | `201 Created` | Inscription réussie, vérifie nom/prénom/email dans le JSON |
| `shouldRejectDuplicateEmail` | `POST /api/auth/register` | `409 Conflict` | Email déjà utilisé |
| `shouldRejectInvalidData` | `POST /api/auth/register` | `400 Bad Request` | Données invalides (email vide, mot de passe court) |
| `shouldReturn404OnLoginUnknownUser` | `POST /api/auth/login` | `404 Not Found` | Utilisateur inexistant |

---

## 6. Annotations utilisées

| Annotation | Rôle |
|---|---|
| `@Testcontainers` | Active le cycle de vie automatique des conteneurs Docker |
| `@Container` | Marque un champ comme conteneur Docker à gérer |
| `@SpringBootTest` | Charge le contexte Spring complet pour les tests |
| `@ActiveProfiles("test")` | Active le profil `test` (→ `application-test.properties`) |
| `@DynamicPropertySource` | Injecte des propriétés dynamiquement (URL JDBC, etc.) |
| `@Transactional` | Rollback automatique de chaque test (isolation) |
| `@BeforeEach` | Exécuté avant chaque méthode de test |
| `@Test` | Marque une méthode comme test JUnit 5 |
| `@DisplayName` | Nom lisible du test dans les rapports |
| `@AutoConfigureMockMvc` | Configure MockMvc pour les tests de contrôleur |

---

## 7. Patron de conception des tests

Chaque test suit le patron **Arrange → Act → Assert** :

```java
@Test
@DisplayName("Sauvegarder et retrouver un User par ID")
void shouldSaveAndFindUserById() {
    // ARRANGE : Les données sont préparées dans @BeforeEach

    // ACT : Exécuter l'opération
    Optional<User> found = userRepo.findById(savedUser.getId());

    // ASSERT : Vérifier le résultat
    assertThat(found).isPresent();
    assertThat(found.get().getNom()).isEqualTo("Dupont");
}
```

**Isolation des tests :**
- `@Transactional` sur chaque classe de test → rollback automatique après chaque test
- `deleteAll()` dans `@BeforeEach` → état propre garanti
- Conteneur PostgreSQL partagé (static) → un seul démarrage pour tous les tests

---

## 8. Prérequis et exécution

### 8.1 Prérequis

- **Java 17+**
- **Maven 3.8+**
- **Docker** en cours d'exécution (requis par Testcontainers)

### 8.2 Commandes d'exécution

```bash
# Exécuter TOUS les tests
./mvnw test

# Exécuter un test spécifique
./mvnw test -Dtest=UserRepoIntegrationTest

# Exécuter tous les tests de repository
./mvnw test -Dtest="com.sge.platforme_etude.repository.*"

# Exécuter tous les tests de service
./mvnw test -Dtest="com.sge.platforme_etude.service.*"

# Exécuter le test du contrôleur
./mvnw test -Dtest=AuthControllerIntegrationTest

# Exécuter avec les logs détaillés
./mvnw test -X
```

### 8.3 Sur Windows (PowerShell)

```powershell
# Avec mvnw.cmd
.\mvnw.cmd test

# Test spécifique
.\mvnw.cmd test "-Dtest=UserRepoIntegrationTest"
```

---

## 9. Statistiques de couverture

| Catégorie | Fichiers | Tests | Assertions (≈) |
|---|---|---|---|
| Repository | 11 | 53 | ~60 |
| Service | 3 | 24 | ~30 |
| Controller | 1 | 4 | ~8 |
| **Total** | **15** | **81** | **~98** |

### Entités couvertes (11/11 = 100%)

- ✅ User
- ✅ Matiere
- ✅ GroupeEtude
- ✅ SessionEtude
- ✅ ObjectifHebdo
- ✅ Invitation
- ✅ Notification
- ✅ Commentaire
- ✅ Disponibilite
- ✅ MessageChat
- ✅ RefreshToken

### Types de vérifications

- ✅ Opérations CRUD complètes
- ✅ Requêtes personnalisées (findBy...)
- ✅ Contraintes d'unicité (email, nom groupe, objectif user+matiere+semaine)
- ✅ Requêtes par plage de dates
- ✅ Vérifications d'existence (existsBy...)
- ✅ Logique d'autorisation (propriétaire, admin)
- ✅ Gestion des exceptions (NotFoundException, ForbiddenException, ConflictException)
- ✅ Validation des données HTTP (400 Bad Request)
- ✅ Endpoints REST publics (register, login)

---

## 10. Résolution de problèmes

### Docker non démarré
```
Could not find a valid Docker environment
```
**Solution :** Démarrer Docker Desktop avant de lancer les tests.

### Port PostgreSQL en conflit
Testcontainers utilise un **port aléatoire** → aucun conflit possible avec une instance PostgreSQL locale.

### Timeout au premier lancement
Le premier lancement télécharge l'image `postgres:16-alpine` (~80 Mo). Les lancements suivants sont plus rapides grâce au cache Docker.

### Test échoué avec "relation does not exist"
Vérifier que `ddl-auto=create-drop` est bien configuré dans `application-test.properties`.
