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

pour supprimer une branche sur le gitlab

```bash
git push origin  --delete NOM_BRANCHE
```

pour supprimer une branche en local

```bash
git brand -D NOM_BRANCHE
```

pour revenir sur un nombbre def de commit

```bash
git revert HEAD~1..HEAD
```

pour revenir sur commit

```bash
git revert NUM COMMIT
```
