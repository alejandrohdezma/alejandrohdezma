{%
laika.template = ../../templates/article.template.html
document.title = Actualizando múltiples repositorios con Scala Steward y GitHub Actions
document.description = "¿Trabajas habitualmente con Scala Steward y gestionas múltiples repositorios Scala? En este post desarrollo una forma de mantener actualizados tus repositorios Scala usando Scala Steward sin volverte loco en un infierno de Pull Requests."
document.spanish = true
document.header = scala_steward_with_github_badge.png
document.date = "2022-01-03"
document.translation.path = blog/updating-multiple-repositories-with-scala-steward-and-github-actions
%}

# Actualizando múltiples repositorios con Scala Steward y GitHub Actions

Si trabajas habitualmente con Scala, probablemente conozcas [Scala Steward].
Si no sabes lo que es, este extracto del propio repositorio lo resume
bastante bien:

> <img src="../../images/scala_steward_avatar.png" width="50" style="float:left; margin: .2em 1em 0 0">
>
> *"Scala Steward is a bot that helps you keep your library
> dependencies, sbt plugins, and Scala and sbt versions up-to-date."*
>
> **Scala Steward es un bot que te ayuda a mantener las dependencias de
> tus librerías, los plugins de SBT y las versiones de Scala y SBT
> actualizadas.**

Se puede utilizar en cualquier proyecto público de Scala hospedado en
GitHub, GitLab o BitBucket que utilice [SBT] o [Mill] simplemente
añadiendo dicho repositorio a [este archivo][repos.json]. Poco tiempo después
empezarás a recibir Pull Requests.

Hay ya más de 1500 repositorios empleando esta instancia pública de
Scala Steward que [Frank Thomas] (el creador de Scala Steward) tiene
desplegado como un servicio gratuito para todo el ecosistema Open-Source
de Scala.

Pero las posibilidades no se quedan ahí, también se puede lanzar una
instancia propia usando [GitHub Actions][scala-steward-action], [Docker] o
incluso [Coursier]:

``` bash
cs launch --contrib scala-steward
```

## El infierno de las actualizaciones ™️

Seguramente, si trabajas en una organización con múltiples repositorios
y con Scala Steward a cargo de mantener todo actualizado, tu equipo
habrá descubierto el conocido como "infierno de las actualizaciones". ¿Y
qué es esto? Pues no es otra cosa que empezar tu jornada laboral
teniendo que revisar, aprobar y mergear cientos de Pull Request de
actualizaciones creadas por Scala Steward.

@:figure(https://media.giphy.com/media/Lopx9eUi34rbq/giphy.gif) {
caption = "Proporcionado por GIPHY"
caption-link = "https://giphy.com/gifs/token-Lopx9eUi34rbq"
}

Si ya has echado un vistazo a las [FAQ] de Scala Steward, habrás visto
que una opción para gestionar esto es mergear automáticamente dichas
actualizaciones usando apps como [Mergify], GitHub Actions como [esta][merge-dependency-update-prs] o
habilitando el [auto-merge] en dichas PR.

Aunque el auto-mergeo puede ser una opción muy válida, es probable que,
como yo, te hayas encontrado que no lo es tanto para tu caso. Bien
porque tu organización no permita el mergeo automático de PR, o quizá
porque tus PR tienen que seguir un flujo concreto que impide que lleguen
a la rama por defecto de tu repositorio (comúnmente `main`
[~o `master`~][no-master]).

Para estos casos, te traigo una solución que (al menos en mi equipo)
está funcionando muy bien. Cómo resumen te diré que consiste en instruir
a Scala Steward a que actualice una rama distinta a la de por defecto de
forma automática (la típica rama `develop`, si sigues [GitFlow]), y
crear una PR desde esa rama a tu rama por defecto cada cierto tiempo.
Ahora bien, ¿cómo hacemos eso? pues con GitHub Actions.

## Scala Steward, olvídate del *main*

El primer paso para huir del infierno de las actualizaciones es decirle
a Scala Steward que en vez de actualizar la rama por defecto de nuestro
repositorio, actualice una rama distinta. Para ello, tenemos dos
opciones, dependiendo de como estemos lanzando Scala Steward.

@:figure(https://media.giphy.com/media/mrBEVU9zQIsZa/giphy.gif) {
caption = "Proporcionado por GIPHY"
caption-link = "https://giphy.com/gifs/off-showed-mrBEVU9zQIsZa"
}

@:details

Si estás usando `repos.json`

Localiza la línea correspondiente a tu repositorio y añade `:rama`
detrás.

``` markdown
- miorganizacion/mirepo:develop
```

@:@

@:details

Si estás usando la [GitHub Action de Scala Steward](https://github.com/scala-steward-org/scala-steward-action)

Añade un nuevo parámetro `branches` a la acción con el nombre de la rama
a actualizar.

``` yaml
- name: Launch Scala Steward
  uses: scala-steward-org/scala-steward-action@v2
  with:
    github-token: ${{ github.token }}
    branches: develop
```

@:@

¡Y ya está! No necesitas hacer nada más para este paso.

## Colega, ¿dónde está mi rama?

Una vez terminado el paso anterior, Scala Steward empezará a enviar PR
actualizando esa rama que le hemos indicado, en vez de la rama por
defecto del repositorio. El problema está en sí, como es lógico, dicha
rama no existe. Para que todo esto funcione, necesitamos asegurarnos de
dos cosas:

-   Por un lado, que exista una rama `develop`.
-   Por otro, que dicha rama se mantenga actualizada con los últimos
    cambios en nuestra rama por defecto.

@:figure(https://media.giphy.com/media/l0MYBtZdU9ZrOiQCc/giphy.gif) {
caption = "Proporcionado por GIPHY"
caption-link = "https://giphy.com/gifs/teamcoco-conan-obrien-ashton-kutcher-l0MYBtZdU9ZrOiQCc"
}

Pues venga, manos a la obra. Vamos a crear un workflow de GitHub Actions
que se encargue de hacer el upsert de la rama `develop`.

> Si no sabes como funciona la sintaxis de GitHub Actions,
> [aquí][github-action-docs] tienes toda la documentación necesaria para
> aprender a utilizarla (en inglés).

Para empezar, creamos un archivo `upsert-develop-branch.yml` en la
carpeta `.github/workflows` de nuestro proyecto. Dentro escribiremos el
esqueleto de un workflow que reaccione a actualizaciones de la rama
`main` (o a como se llame tu rama por defecto).

``` yaml
name: Upsert `develop` branch

on:
  push:
    branches: main

jobs:
  upsert-develop-branch:
    runs-on: ubuntu-latest
    name: Create `develop` branch or rebase it
    steps:
      - run: echo "Upsert develop branch"
```

Ahora añadiremos los pasos.

El primer paso consistirá en asegurarnos que la rama `develop` existe.
Para ello vamos a usar la [CLI de GitHub][gh] (`gh`).

``` yaml
- name: Create `develop` branch (if it does not exists)
  env:
    GITHUB_TOKEN: ${{ github.token }}
  run: |
      gh api --silent \
          /repos/${{ github.repository }}/git/refs \
          -f ref="refs/heads/develop" \
          -f sha="${{ github.sha}}" ||
          echo '`develop` branch already exists on ${{ github.repository }}'
```

Creamos una referencia a la rama `develop` en el mismo punto (SHA) en el
que esté la rama `main` (que vendrá en el contexto `github.sha`). Si el
comando fallase, significa que la rama `develop` ya existe, por lo que
avisamos al usuario por pantalla para evitar que el workflow entero
falle.

> `gh` nos permite hacer múltiples operaciones en GitHub desde la
> consola, de una forma sencilla y concisa. Si quieres saber más sobre
> los distintos comandos, te animo a que consultes [su documentación][gh-docs]
> (en inglés).

El siguiente paso será hacer checkout de la rama `develop`. Debemos
añadir `fetch-depth: 0` para evitar que el workflow haga un
[`shallow-clone`][shallow-clone].

``` yaml
- name: Checkout develop branch
  uses: actions/checkout@v2
  with:
    ref: develop
    fetch-depth: 0
```

Por último, añadimos el paso que se encarga de hacer el rebase de la
rama `develop`. Si el primer paso creó una rama nueva, este paso no hará
nada; si ya existía previamente, este paso la rebaseará a la última
posición de `main`.

``` yaml
- name: Rebase `develop` branch to latest `origin/main`
  run: |
    git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
    git config user.name "github-actions[bot]"
    git rebase origin/main
    git push -f -u origin develop
```

> Necesitamos establecer las opciones de `user.email` y `user.name` de
> Git puesto que estas [no se inicializan][checkout-gitconfig-issue] cuando se 
> usa la acción de checkout.

¡Y ya está! Ya tenemos nuestro primer workflow terminado. Aquí tienes el
código completo:

@:details

`.github/workflows/upsert-develop-branch.yml`

``` yaml
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

@:@

Una vez añadamos este archivo a nuestro repositorio cualquier cambio en
la rama `main` provocará que la rama `develop` se cree o actualice.

A partir de este punto, podemos empezar a recibir PR de Scala Steward a
la rama `develop`. Únicamente necesitaremos que dichas PR se mergeen de
forma automática. Para ello, como ya se mencionó en la primera sección,
podemos usar apps como [Mergify], GitHub Actions como
[esta][merge-dependency-update-prs] o habilitar el [auto-merge] en dichas PR
usando un nuevo workflow, lo dejo a tu elección.

@:figure(https://media.giphy.com/media/TbYgHMnICI1A4/giphy.gif) {
caption = "Proporcionado por Giphy"
caption-link = "https://giphy.com/gifs/dragon-ball-z-dbz-TbYgHMnICI1A4"
}

## El día de la actualización

Llegados a esté punto, tendremos nuestra rama `develop` cargada de
actualizaciones. Así que lo único que falta es asegurarnos de que cada
cierto tiempo, se cree una PR a nuestra rama por defecto.

¿Y cómo podemos hacer eso? ¡Pues claro! Otra vez usando GitHub Actions.
¡Al lío!

Primero de todo, igual que antes, crearemos un archivo
`scheduled-updates-pr.yml` en la carpeta `.github/workflows` de nuestro
proyecto. La diferencia con el workflow que escribimos en el paso
anterior estará en que en vez de reaccionar a actualizaciones de `main`
haremos que este workflow se lance una y otra vez, en los intervalos que
designemos usando el evento [`schedule`][schedule].

> En el ejemplo se establece que el workflow se lance semanalmente. Si
> quieres cambiar esta programación puedes servirte de
> [esta página][crontab.guru] para calcular tu comando CRON.

``` yaml
name: Create PR from `develop` to `main`

on:
  schedule:
    - cron: "0 0 * * MON"

jobs:
  create-develop-pr:
    runs-on: ubuntu-latest
    name: Create PR from `develop` to `main`
    steps:
      - run: echo "Create develop PR"
```

Este workflow arrancará todos los lunes a las 00:00.

@:figure(https://media.giphy.com/media/3o7WIQ4FARJdpmUni8/giphy-downsized-large.gif) {
caption = "Proporcionado por Giphy"
caption-link = "https://giphy.com/gifs/bill-murray-groundhog-day-well-its-again-3o7WIQ4FARJdpmUni8"
}

Y ahora vamos a por la implementación. Consistirá en un único paso que
hará lo siguiente:

-   Buscar el SHA de la rama `main`.
-   Buscar el SHA de la rama `develop`.
-   Si los SHA no coinciden, creará una PR de `develop` a `main`.
-   Si los SHA coinciden, informará de que no hay actualizaciones.

``` yaml
- name: Create Pull Request
  run: |
    develop=$(gh api /repos/$GITHUB_REPOSITORY/commits/refs/heads/develop -q '.sha')
    main=$(gh api /repos/$GITHUB_REPOSITORY/commits/refs/heads/main -q '.sha')

    if [[ $develop != $main ]]; then
        gh api /repos/$GITHUB_REPOSITORY/pulls \
          -f title="Scala Steward Updates" \
          -f base=main \
          -f head=develop
    else
        echo "There are no updates"
    fi
  env:
    GITHUB_TOKEN: ${{ github.token }}
```

¡Hecho! Nuestro nuevo workflow no necesita nada más. Aquí tienes la
versión completa:

@:details

`.github/workflows/scheduled-updates-pr.yml`

``` yaml
name: Create PR from `develop` to `main`

on:
  schedule:
    - cron: "0 0 * * MON"

jobs:
  create-develop-pr:
    runs-on: ubuntu-latest
    name: Create PR from `develop` to `main`
    steps:
      - name: Create Pull Request
        run: |
          develop=$(gh api /repos/$GITHUB_REPOSITORY/commits/refs/heads/develop -q '.sha')
          main=$(gh api /repos/$GITHUB_REPOSITORY/commits/refs/heads/main -q '.sha')

          if [[ $develop != $main ]]; then
              gh api /repos/$GITHUB_REPOSITORY/pulls \
                -f title="Scala Steward Updates" \
                -f base=main \
                -f head=develop
          else
              echo "There are no updates"
          fi
        env:
          GITHUB_TOKEN: ${{ github.token }}
```

@:@

Y... ¡listo! Con estos dos workflows que acabamos de crear ya tenemos
todo lo necesario para evitar "El infierno de las actualizaciones ™️".
Podemos replicar esto mismo en todos los repositorios Scala de nuestra
organización y mantenerlos actualizados sin demasiado esfuerzo.

Te dejo aquí algunas mejoras que puedes implementar por si te apetece
cacharrear con GitHub Actions:

-   Se podría enviar una notificación al Slack (o a la app que uséis en
    vuestra organización para comunicaros) cuando el rebase automático
    de la rama `develop` falle.
-   Activar una [protección de rama][branch-protection] sobre la rama `develop`
    para asegurar que las PR siempre se prueban encima de todas las 
    actualizaciones que ya hayan tenido lugar.
-   ¿Se te ocurre algo más? ¡Déjame un comentario!

[Scala Steward]: https://github.com/scala-steward-org/scala-steward
[SBT]: https://www.scala-sbt.org
[Mill]: https://com-lihaoyi.github.io/mill/mill/Intro_to_Mill.html
[repos.json]: https://github.com/scala-steward-org/repos/blob/main/repos-github.md
[Frank Thomas]: https://github.com/fthomas
[scala-steward-action]: https://github.com/scala-steward-org/scala-steward-action
[Docker]: https://hub.docker.com/r/fthomas/scala-steward/
[Coursier]: https://get-coursier.io
[FAQ]: https://github.com/scala-steward-org/scala-steward/blob/master/docs/faq.md#how-can-scala-stewards-prs-be-merged-automatically
[Mergify]: https://mergify.com
[merge-dependency-update-prs]: https://github.com/marketplace/actions/merge-dependency-update-prs
[auto-merge]: https://docs.github.com/es/pull-requests/collaborating-with-pull-requests/incorporating-changes-from-a-pull-request/automatically-merging-a-pull-request
[no-master]: https://twitter.com/mislav/status/1270388510684598272
[GitFlow]: https://nvie.com/posts/a-successful-git-branching-model/
[github-action-docs]: https://docs.github.com/en/actions/quickstart
[gh]: https://cli.github.com
[gh-docs]: https://cli.github.com/manual/
[shallow-clone]: https://git-scm.com/docs/git-clone#Documentation/git-clone.txt---depthltdepthgt
[checkout-gitconfig-issue]: https://github.com/actions/checkout/issues/13#issuecomment-724415212
[schedule]: https://docs.github.com/es/actions/using-workflows/events-that-trigger-workflows#schedule
[crontab.guru]: https://crontab.guru
[branch-protection]: https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/defining-the-mergeability-of-pull-requests/about-protected-branches#require-status-checks-before-merging
