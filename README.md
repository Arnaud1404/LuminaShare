# LuminaShare - Application de Partage de Photos

Une application de partage de photos développée par Arnaud Gomes, Kamiel De Vos et Soraya Benachour.

## Prérequis

- Java 17
- Maven
- PostgreSQL
- Node.js et npm (pour le frontend)

## Instructions de Configuration

1. Créez un fichier `.env` à la racine avec les variables suivantes:

   ```
   DATABASE_NAME=<nom>
   DATABASE_PASSWORD=<motdepasse>
   DATABASE_PATH=pgsql # au CREMI, ou localhost pour un environnement local
   DATABASE_TABLE=imageDatabase # nom de la table pour stocker les images
   ```

### Localement

2. Installez et lancez l'application:
   ```
   mvn clean install
   mvn --projects backend spring-boot:run
   ```

### Avec Docker

2. Construisez et lancez l'application:

```bash
# Construire les images
sudo docker compose build

# Lancer l'application
sudo docker compose up -d

# Voir les logs
sudo docker compose logs app

# Arrêter l'application
sudo docker compose down
```

3. Accédez à l'application: http://localhost:8181 ou http://localhost:8182 (Docker)

4. Obtenir des images de test (Optionnel)
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

## Inspiration

Données : https://www.dcode.fr/donnees-exif
Langue  : https://www.youtube.com/watch?v=_QTRUA7bZvU&t=303s
routeur : https://www.youtube.com/watch?v=fvr1E8SWFrg&t=932s

## Testé Sur

### Systèmes d'exploitation

- Ubuntu 22.04 LTS
- Debian Bookworm

### Navigateurs

- Google Chrome
- Mozilla Firefox
- Opera
