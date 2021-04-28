# Ktoggl CLI

**Ktoggl CLI** vous permet de générer vos timesheets / fichiers de pointages via le logiciel [**Toggl Track**](https://toggl.com/track/toggl-desktop/).

**Ktoggl CLI n'est pas affilié à la marque / société / logiciel Toggl.**

## Fonctionnalités

### Export pointages **SAP CATS** 🐈

![](./readme-resources/toggl-to-sap.png)

⚠️ La seule contrainte de cette feature est que les informations des projets pointés doivent se trouver dans le titre du
projet. En effet, avec la version gratuite de **Toggl**, vous ne pouvez pas avoir accès aux **Tasks**. Prenez cela comme
un workaround 🤓

Les projets dans **Ktoggl CLI** doivent être sous cette
forme : `id_projet description_projet id_tâche description_tâche` pour représenter au mieux le fonctionnement de **SAP CATS**.

Si un nom de client est disponible, il faut le rajouter aussi (*Attention à la casse !*) :

![](./readme-resources/project-formating-example.png)

C'est la seule manipulation nécessaire en amont pour que la génération du fichier *Excel* fonctionne.

Exemple de commande pour générer ce fichier :

- Avec le fichier **JAR** :

```shell
$ java -jar ktoggl-cli-0.0.2.jar sap --api-key <votre_clef_api_toggl> --workspace <le_workspace_id_ciblé> --since 2021-04-15 -g
```

- Avec **DockerHub** [Ktoggl-CLI](https://hub.docker.com/r/remylavergne/ktoggl-cli) *(prendre la dernière version)* 🐳:

```shell
$ docker pull remylavergne/ktoggl-cli:0.0.2

$ docker run -it --rm -v $PWD/output:/usr/src/ktoggl/ktoggl-cli-output remylavergne/ktoggl-cli:0.0.2


bash-4.4# ktoggl-cli sap --api-key <votre_clef_api_toggl> --workspace <le_workspace_id_ciblé> --since 2021-04-15 -g
```

- Avec le **Dockerfile** *(prendre la dernière version)* :

```shell
$ docker build --no-cache --rm -t remylavergne/ktoggl-cli:0.0.2 .

$ docker run -it --rm -v $PWD/output:/usr/src/ktoggl/ktoggl-cli-output remylavergne/ktoggl-cli:0.0.2

bash-4.4# ktoggl-cli sap --api-key <votre_clef_api_toggl> --workspace <le_workspace_id_ciblé> --since 2021-04-15 -g
```

`--api-key`, `-a` : Votre clef API se trouve dans vos paramètres de compte Toggl <br />
`--workspace`, `-w` : L'id du workspace visé (se trouve facilement dans l'url)<br />
`--since`, `-s` : Date de début pour la récupération des données<br />
`--until`, `-u` : Date de fin pour la récupération des données (si vide, le jour actuel est pris en compte)<br />
`-g` / `--no-group` : Grouper / ne pas grouper les mêmes projets sur une journée<br />

A la fin du processus, des logs permettent d'avoir un aperçu de ce qui a été généré, avec les différences de temps en
plus, ou en moins.

Exemple de résultat (⚠️ ceci risque de changer dans le temps) :

```bash
-> (2021-04-05 to 2021-04-22) Excel generation done with 100 entries

-> Summary
14 days exported
Total: 110.0 hours
Total expected: 112 hours

-> Differences based on 8 hours shift / day:
- 2021-04-22: - 1.0
- 2021-04-21: - 1.5
- 2021-04-20: + 0.25
- 2021-04-19: - 0.25
- 2021-04-16: + 0.25
- 2021-04-15: + 0.5
- 2021-04-12: + 0.5
- 2021-04-09: - 0.25
- 2021-04-08: - 0.25
- 2021-04-06: + 2.5
- 2021-04-05: - 7.25
```

Le fichier Excel généré est directement importable dans **SAP CATS**. Vous pouvez ainsi ajuster vos pointages avec le
compte rendu.

![](./readme-resources/excel-export-example.png)

Vous remarquerez que les heures ont été arrondies au quart supérieur, ou inférieur, pour respecter le standard de
pointage de **SAP CATS**.

Vous pouvez maintenant uploader cet Excel dans votre **SAP CATS**.
