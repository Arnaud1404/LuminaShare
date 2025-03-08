# LuminaShare - Application de Partage de Photos

Une application de partage de photos développée par Arnaud Gomes, Kamiel De Vos et Soraya Benachour.

## Instructions de Configuration

1. Créez un fichier `.env` dans le dossier backend avec les variables suivantes:

   ```
   DATABASE_NAME=<nom>
   DATABASE_PASSWORD=<motdepasse>
   DATABASE_PATH=localhost
   ```

2. Installez et lancez l'application:
   ```
   mvn clean install
   mvn --projects backend spring-boot:run
   ```
