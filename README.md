# Table of Contents

[[_TOC_]]

# Projet L3

Ceci est un projet developper par De Vos Kamiel, Arnaud Gomes

# Installation

pour installer les dépendances

```bash
cmd à mettre
```

après avoir installer les dépendances, lancer la commande

```bash
mvn clean install
```

```bash
mvn --projects backend spring-boot:run
```

pour généré la doc

```bash
à def
```

# Quelque raccourci

pour prévisualiser le markdown
Ctrl + Shift + v

# some git command

pour commit directement en lien avec les issues

```bash
git commit -am "description #NUM"
```

si on souhaite directement fermer une issue on peut faire

```bash
git commit -am "description NOM#NUM"
```

Nom = Close | Fix | Resolve | Implemented

[patern](https://docs.gitlab.com/user/project/issues/managing_issues/#default-closing-pattern)

pour afficher le dernier commit

```bash
git log | head
```

pour créer une nouvelle branche

```bash
git checkout -b NOM_BRANCHE
```

pour changer de branche

```bash
git checkout NOM_BRANCHE
```

pour récup le main local dans une branche

```bash
git merge NOM_BRANCHE
```

la manip en entier (exemple)

```bash
git checkout develop_kdevos
git merge main
git push
```

BDD :
Descripteurs de fichiers
Contenu du JSON :

- Id : L’identifiant auquel est accessible l’image (type long)
- Name : Le nom du fichier qui a servi à construire l’image (type string)
- Type : Le type de l’image (type org.springframework.http.MediaType)
- Size : Une description de la taille de l’image, par exemple 640\*480 pour une image de
  640 × 480 pixels (type string)

Serveur :
Contenu du JSON + Contenu des images
à tout moment, chaque image du serveur doit être indexée dans la BDD
