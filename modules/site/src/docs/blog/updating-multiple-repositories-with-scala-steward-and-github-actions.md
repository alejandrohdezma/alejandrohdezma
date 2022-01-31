{%
	laika.template = ../../templates/article.template.html
	document.title = Updating multiple repositories with Scala Steward and GitHub Actions
    document.description = "Do you regularly work with Scala Steward and manage multiple Scala repositories? In this post I describe a way to keep your Scala repositories up to date using Scala Steward without going crazy in a hell of Pull Requests."
	document.spanish = false
    document.header = scala_steward_with_github_badge.png
    document.date = "2022-01-03"
    document.translation.path = blog/actualizando-multiples-repositorios-con-scala-steward-y-github-actions
%}

# Updating multiple repositories with Scala Steward and GitHub Actions

If you regularly work with Scala, you probably know [Scala Steward].
If you don't know what it is, this excerpt from the repository itself summarizes it
pretty well:

> <img src="../../images/scala_steward_avatar.png" width="50" style="float:left; margin: .2em 1em 0 0">
>
> *"Scala Steward is a bot that helps you keep your library
> dependencies, sbt plugins, and Scala and sbt versions up-to-date."*

It can be used on any public Scala project hosted on
GitHub, GitLab or BitBucket that uses [SBT] or [Mill] by simply
adding said repository to [this file][repos.json]. Shortly after that
you will start receiving Pull Requests.

There are already more than 1500 repositories using this public instance of
Scala Steward which [Frank Thomas] (the creator of Scala Steward) has
deployed as a free service for the entire Scala Open-Source ecosystem.

But the possibilities don't stop there, you can also launch your own instance
using [GitHub Actions][scala-steward-action], [Docker] or even [Coursier]:

``` bash
cs launch --contrib scala-steward
```

## *Update Hell ™️*

Most likely, if you work in an organization with multiple repositories and
Scala Steward in charge of keeping everything up to date, your team will have
discovered what is known as *Update Hell ™️*. And what is this? Well, it's nothing
more than starting your workday having to review, approve and merge hundreds of
Pull Requests for updates created by Scala Steward.

@:figure(https://media.giphy.com/media/Lopx9eUi34rbq/giphy.gif) {
caption = "Provided by Giphy"
caption-link = "https://giphy.com/gifs/token-Lopx9eUi34rbq"
}

If you have already taken a look at the Scala Steward [FAQ], you may have seen
that one option to handle these is to auto-merge such updates using apps like
[Mergify], GitHub Actions like [this][merge-dependency-update-prs] or by
enabling [auto-merge] on those PRs. 

While auto-merging may be a very valid option, it is likely that, like me, you
have encountered that it is not so valid in your case. Either because your
organization does not allow auto-merging of PRs, or perhaps because your PRs
have to follow a particular flow that prevents them from reaching the default
branch of your repository (commonly `main` [~~or `master`~~][no-master]).

For these cases, I offer you a solution that (at least in my team) is working
very well. As a brief summary, it consists on instructing Scala Steward to
update a branch other than the default one automatically (the typical `develop`
branch, if you follow [GitFlow]), and create a PR from that branch to your
default branch periodically. Now, how do we do that? Well, with GitHub Actions.

## Scala Steward, forget about *main*

The first step to escape *Update Hell ™️* is to tell Scala Steward to use a
different branch instead of updating the default branch of our repository. To
do this, we have two options, depending on how we are launching Scala Steward.

@:figure(https://media.giphy.com/media/mrBEVU9zQIsZa/giphy.gif) {
caption = "Provided by Giphy"
caption-link = "https://giphy.com/gifs/off-showed-mrBEVU9zQIsZa"
}

@:details

If you are using `repos.json`

Locate the line corresponding to your repository and add `:branch` at the end.

``` markdown
- myorg/myrepo:develop
```

@:@

@:details

If you are using [Scala Steward's GitHub Action](https://github.com/scala-steward-org/scala-steward-action)

Add a new `branches` parameter to the action's input with the name of the
branch to update.

``` yaml
- name: Launch Scala Steward
  uses: scala-steward-org/scala-steward-action@v2
  with:
    github-token: ${{ github.token }}
    branches: develop
```

@:@

And that's it! You don't need to do anything else for this step.

## Dude, where is my branch?

Once the above step is finished and deployed, Scala Steward will start sending
PRs updating the branch we have indicated, instead of the default branch of the
repository. The problem is that, as you might expect, such a branch does not
exist. For all this to work, we need to ensure two things:

-   On the one hand, that there is a `develop` branch.
-   On the other hand, that this branch is kept up to date with the latest
    changes in our default branch.

@:figure(https://media.giphy.com/media/l0MYBtZdU9ZrOiQCc/giphy.gif) {
caption = "Provided by Giphy"
caption-link = "https://giphy.com/gifs/teamcoco-conan-obrien-ashton-kutcher-l0MYBtZdU9ZrOiQCc"
}

Well then, let's get to work. We are going to create a GitHub Actions workflow
that takes care of upserting the `develop` branch.

> If you don't know how GitHub Actions syntax works, [here][github-action-docs]
> you have all the necessary documentation to learn how to use it.

We start by creating a file named `upsert-develop-branch.yml` inside the
`.github/workflows` folder of our project. Here we will write the skeleton of
a workflow that reacts to updates of the `main` branch (or whatever your default
branch is called).

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

Now lets add the steps.

The first step will consist of making sure that the `develop` branch exists. To
do this we will use the [GitHub CLI][gh] (`gh`).

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

We create a reference to the `develop` branch at the same spot (SHA) where the
`main` branch is (which will be in the `github.sha` context). If the command
fails, it means that the `develop` branch already exists, so we warn the user
via `echo` to prevent the whole workflow from failing.

> `gh` allows us to do multiple operations in GitHub directly from the shell,
> in a simple and concise way. If you want to know more about the different
> commands, I recommend you to consult [its documentation][gh-docs].

The next step will be to checkout the `develop` branch. We must add
`fetch-depth: 0` to prevent the workflow from doing a
[`shallow-clone`][shallow-clone].

``` yaml
- name: Checkout develop branch
  uses: actions/checkout@v2
  with:
    ref: develop
    fetch-depth: 0
```

Finally, we add the step that takes care of rebasing the `develop` branch. If
the first step created a new branch, this step will do nothing; if it already
exists, this step will rebase it to `main`'s head.

``` yaml
- name: Rebase `develop` branch to latest `origin/main`
  run: |
    git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
    git config user.name "github-actions[bot]"
    git rebase origin/main
    git push -f -u origin develop
```

> We need to set Git's `user.email` and `user.name` settings since these
> [are not initialized][checkout-gitconfig-issue] when the checkout action
> is used.

And that's it! We already have completed our first workflow. Here you have the
full code:

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

Once we add this file to our repository any push to the `main` branch will
cause the `develop` branch to be created or updated.

From this point on, we can start receiving PRs from Scala Steward to the
`develop` branch. We will only need to merge those PRs automatically. To do
this, as mentioned in the first section, we can use apps like [Mergify],
GitHub Actions like [this one][merge-dependency-update-prs] or enable
[auto-merge] on those PRs using a new workflow, I leave it up to you.

@:figure(https://media.giphy.com/media/TbYgHMnICI1A4/giphy.gif) {
caption = "Provided by Giphy"
caption-link = "https://giphy.com/gifs/dragon-ball-z-dbz-TbYgHMnICI1A4"
}

## Update day

By now, we will have our `develop` branch loaded with updates. So the only thing
left to do is to make sure that every so often, a PR is created to our
default branch.

And how can we do that? Of course! Using GitHub Actions, let's get to it!

First of all, as before, we will create a new file `scheduled-updates-pr.yml`
in the `.github/workflows` folder. The difference with the previous workflow
will be that instead of reacting to pushes to `main` we will make this workflow
launch over and over again, at the intervals we designate using the
[`schedule`][schedule] event.

> In this example the workflow is set to be launched weekly. If you want to
> change this schedule you can use [this page][crontab.guru] to calculate your
> CRON command.

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

This workflow will start every Monday at 00:00.

@:figure(https://media.giphy.com/media/3o7WIQ4FARJdpmUni8/giphy-downsized-large.gif) {
caption = "Provided by Giphy"
caption-link = "https://giphy.com/gifs/bill-murray-groundhog-day-well-its-again-3o7WIQ4FARJdpmUni8"
}

And now let's go for the implementation. It will consist of a single step that will do the following:

-   Search for the `main` branch SHA.
-   Search for the `develop` branch SHA.
-   If the SHAs do not match, it will create a PR from `develop` to `main`.
-   If the SHA's match, it will report that there are no updates.

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

Done! Our new workflow doesn't need anything else. Here is the full version:

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

And... that's it! With these two workflows we just created we have everything
we need to avoid *Update Hell ™️*. We can replicate this approach in all of our organization's Scala repositories and keep them up to date without too much
effort.

Here are some improvements you can implement in case you feel like playing
around with GitHub Actions:

-   A notification could be sent to Slack (or whatever app your organization
    uses to communicate) when the automatic rebasing of the `develop` branch fails.
-   Activate a [branch-protection][branch-protection] on the `develop` branch
    to ensure that PRs are always tested on top of all updates that have already
    taken place.
-   Can you think of anything else? Leave me a comment!

Thank you and see you next time!

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
[schedule]: https://docs.github.com/en/actions/using-workflows/events-that-trigger-workflows#schedule
[crontab.guru]: https://crontab.guru
[branch-protection]: https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/defining-the-mergeability-of-pull-requests/about-protected-branches#require-status-checks-before-merging
