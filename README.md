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
   DATABASE_PATH=pgsql # pour le CREMI, ou localhost pour un environnement local
   ```

2. Installez et lancez l'application:
   ```
   mvn clean install
   mvn --projects backend spring-boot:run
   ```

## Testé Sur

### Systèmes d'exploitation

- Ubuntu 22.04 LTS
- Debian Bookworm

### Navigateurs

- Google Chrome
- Mozilla Firefox
