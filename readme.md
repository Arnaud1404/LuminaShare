# Projet L3
Ceci est un projet developper par De Vos Kamiel, Arnaud Gomes

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
