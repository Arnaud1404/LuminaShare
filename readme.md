# some git command
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


Pour prévisualiser le Markdown : Ctrl + Shift + b
