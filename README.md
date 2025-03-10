# LuminaShare - Application de Partage de Photos

Une application de partage de photos développée par Arnaud Gomes, Kamiel De Vos et Soraya Benachour.

## Prérequis

- Java 17
- Maven
- PostgreSQL
- Node.js et npm (pour le frontend)

## Instructions de Configuration

1. Créez un fichier `.env` dans le dossier `backend` avec les variables suivantes:

   ```
   DATABASE_NAME=<nom>
   DATABASE_PASSWORD=<motdepasse>
   DATABASE_PATH=localhost # pour un environnement local, ou pgsql au CREMI
   ```

2. Installez et lancez l'application:
   ```
   mvn clean install
   mvn --projects backend spring-boot:run
   ```

## Documentation

Pour générer la documentation Javadoc, exécutez la commande suivante:

```
mvn javadoc:javadoc
```

La documentation sera disponible dans backend/target/site/javadoc/index.html

Si vous n'avez pas Javadoc d'installé, installez tout le JDK

```
Sur Linux: `sudo apt-get install openjdk-17-jdk
```

## Testé Sur

### Systèmes d'exploitation

- Ubuntu 22.04 LTS
- Debian Bookworm

### Navigateurs

- Google Chrome
- Mozilla Firefox
