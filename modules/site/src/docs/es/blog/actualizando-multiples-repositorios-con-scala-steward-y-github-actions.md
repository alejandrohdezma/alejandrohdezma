{%
	laika.template = ../../templates/article.template.html
	document.title = Actualizando múltiples repositorios con Scala Steward y GitHub Actions
    document.description = "¿Trabajas habitualmente con Scala Steward y gestionas múltiples repositorios Scala? En este post desarrollo una forma de mantener actualizados tus repositorios Scala usando Scala Steward sin volverte loco en un infierno de Pull Requests."
	document.spanish = true
    document.header = scala_steward_with_github_badge.png
    document.date = "2022-01-03"
    document.translation.path = blog/updating-multiple-repositories-with-scala-steward-and-github-actions.html
%}

# Actualizando múltiples repositorios con Scala Steward y GitHub Actions

Si trabajas habitualmente con Scala, probablemente conozcas [Scala Steward](https://github.com/scala-steward-org/scala-steward). Si no sabes lo que es, este extracto del propio repositorio lo resume bastante bien:

> <img src="../../images/scala_steward_avatar.png" width="50" style="float:left; margin: .2em 1em 0 0">
> 
> _"Scala Steward is a bot that helps you keep your library dependencies, sbt plugins, and Scala and sbt versions up-to-date."_
> 
> **Scala Steward es un bot que te ayuda a mantener las dependencias de tus librerías, los plugins de SBT y las versiones de Scala y SBT actualizadas.**

Se puede utilizar en cualquier proyecto público de Scala hospedado en GitHub, GitLab o BitBucket que utilice [SBT](https://www.scala-sbt.org) o [Mill](https://com-lihaoyi.github.io/mill/mill/Intro_to_Mill.html) simplemente añadiendo dicho repositorio a [este archivo](https://github.com/scala-steward-org/repos/blob/main/repos-github.md). Poco tiempo después empezarás a recibir "Pull Requests".

Hay ya más de 1500 repositorios empleando esta instancia pública de Scala Steward que [Frank Thomas](https://github.com/fthomas) (el creador de Scala Steward) tiene desplegado como un servicio gratuito para todo el ecosistema "Open-Source" de Scala.

Pero las posibilidades no se quedan ahí, también se puede lanzar una instancia propia usando [GitHub Actions](https://github.com/scala-steward-org/scala-steward-action), [Docker](https://hub.docker.com/r/fthomas/scala-steward/) o incluso [Coursier](https://get-coursier.io):

```language-bash
cs launch --contrib scala-steward
```

## El infierno de las actualizaciones

Seguramente, si trabajas en una organización con múltiples repositorios y con Scala Steward a cargo de mantener todo actualizado, tu equipo habrá descubierto el conocido como "infierno de las actualizaciones". ¿Y qué es esto? Pues no es otra cosa que empezar tu jornada laboral teniendo que revisar, aprobar y "mergear" cientos de "Pull Request" de actualizaciones creadas por Scala Steward.

<figure>
  <img src="https://media.giphy.com/media/Lopx9eUi34rbq/giphy.gif" />
  <figcaption><a rel="external" target="_blank" href="https://giphy.com/gifs/token-Lopx9eUi34rbq">Proporcionado por GIPHY</a></figcaption>
</figure>

Si ya has echado un vistazo a las "[FAQ](https://github.com/scala-steward-org/scala-steward/blob/master/docs/faq.md#how-can-scala-stewards-prs-be-merged-automatically)" de Scala Steward, habrás visto que una opción para gestionar esto es "mergear" automáticamente dichas actualizaciones usando apps como [Mergify](https://mergify.com), GitHub Actions como [esta](https://github.com/marketplace/actions/merge-dependency-update-prs) o habilitando el "[auto-merge](https://docs.github.com/es/pull-requests/collaborating-with-pull-requests/incorporating-changes-from-a-pull-request/automatically-merging-a-pull-request)" en dichas "PR".

Aunque el "auto-mergeo" puede ser una opción muy válida, es probable que, como yo, te hayas encontrado que no lo es tanto para tu caso. Bien porque tu organización no permita el "mergeo" automático de "PR", o quizá porque tus "PR" tienen que seguir un flujo concreto que impide que lleguen a la rama por defecto de tu repositorio (comúnmente `main` [<del>o `master`</del>](https://twitter.com/mislav/status/1270388510684598272)).

Para estos casos, te traigo una solución que (al menos en mi equipo) está funcionando muy bien. Cómo resumen te diré que consiste en instruir a Scala Steward a que actualice una rama distinta a la de por defecto de forma automática (la típica rama `develop`, si sigues "[GitFlow](https://nvie.com/posts/a-successful-git-branching-model/)"), y crear una "PR" desde esa rama a tu rama por defecto cada cierto tiempo. Ahora bien, ¿cómo hacemos eso? pues con GitHub Actions.

## Scala Steward, olvídate del _main_

El primer paso para huir del infierno de las actualizaciones es decirle a Scala Steward que en vez de actualizar la rama por defecto de nuestro repositorio, actualice una rama distinta. Para ello, tenemos dos opciones, dependiendo de como estemos lanzando Scala Steward.

<figure>
  <img src="https://media.giphy.com/media/mrBEVU9zQIsZa/giphy.gif" />
  <figcaption><a rel="external" target="_blank" href="https://giphy.com/gifs/off-showed-mrBEVU9zQIsZa">Proporcionado por GIPHY</a></figcaption>
</figure>

<details><summary>Si estás usando `repos.json`</summary>

<p>Localiza la línea correspondiente a tu repositorio y añade <code>:rama</code> detrás.</p>
<pre>- miorganizacion/mirepo:develop</pre>

</details>

<details><summary>Si estás usando la <a href="https://github.com/scala-steward-org/scala-steward-action" target="_blank" rel="external">GitHub Action de Scala Steward</a></summary>

<p>Añade un nuevo parámetro <code>branches</code> a la acción con el nombre de la rama a actualizar.</p>
<pre>
- name: Launch Scala Steward
  uses: scala-steward-org/scala-steward-action@v2
  with:
    github-token: ${{ github.token }}
    branches: develop
</pre>

</details>

¡Y ya está! No necesitas hacer nada más para este paso.

## Colega, ¿dónde está mi rama?

Una vez terminado el paso anterior, Scala Steward empezará a enviar PR actualizando esa rama que le hemos indicado, en vez de la rama por defecto del repositorio. El problema está en sí, como es lógico dicha rama no existe. Para que todo esto funcione, necesitamos asegurarnos de dos cosas:

- Por un lado, que exista una rama `develop`.
- Por otro, que dicha rama se mantenga actualizada con los últimos cambios en nuestra rama por defecto.

<figure>
  <img src="https://media.giphy.com/media/l0MYBtZdU9ZrOiQCc/giphy.gif" />
  <figcaption><a rel="external" target="_blank" href="https://giphy.com/gifs/teamcoco-conan-obrien-ashton-kutcher-l0MYBtZdU9ZrOiQCc">Proporcionado por GIPHY</a></figcaption>
</figure>


Pues venga, manos a la obra. Vamos a crear un "workflow" de GitHub Actions que se encargue de hacer el "upsert" de la rama `develop`.

```language-yaml
name: Upsert `develop` branch

on:
  push:
    branches: main

jobs:
  upsert-develop-branch:
    runs-on: ubuntu-latest
    name: Create `develop` branch or rebase it to latest `main`
    steps:
      - name: Create `develop` branch (if it does not exists)
        env:
          GITHUB_TOKEN: ${{ github.token }}
        run: |
            gh api --silent \
                /repos/${{ github.repository }}/git/refs \
                -f ref="refs/heads/develop" \
                -f sha="${{ github.sha}}" ||
                echo '`develop` branch already exists on ${{ github.repository }}'
      - name: Checkout develop branch
        uses: actions/checkout@v2
        with:
          ref: develop
          fetch-depth: 0

      - name: Rebase `develop` branch to latest `origin/main`
        run: |
            git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
            git config user.name "github-actions[bot]"
            git rebase origin/main
            git push -f -u origin develop
```
