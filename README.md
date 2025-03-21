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
   DATABASE_PATH=pgsql # au CREMI, ou localhost pour un environnement local
   DATABASE_TABLE=imageDatabase # nom de la table pour stocker les images
   ```

2. Installez et lancez l'application:
   ```
   mvn clean install
   mvn --projects backend spring-boot:run
   ```
3. Obtenir des images de test (Optionnel)
   https://picsum.photos/images
## Documentation

Pour générer la documentation Javadoc, exécutez la commande suivante:

```
mvn javadoc:javadoc
```

La documentation sera disponible dans `docs/api/index.html`

Si vous n'avez pas Javadoc d'installé, installez tout le JDK

```
Sur Ubuntu/Debian:
sudo apt-get install openjdk-17-jdk
```

## Testé Sur

### Systèmes d'exploitation

- Ubuntu 22.04 LTS
- Debian Bookworm

### Navigateurs

- Google Chrome
- Mozilla Firefox
