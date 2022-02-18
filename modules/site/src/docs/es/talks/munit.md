{%
	laika.template = ../../templates/talk.template.html
	document.title = "Tu futuro test-framework favorito: MUnit"
  document.description = "“¡Hey! ¿Quieres conocer el nuevo test-framework para Scala del que todo el mundo está hablando?“, “¡Estarás de broma! ¿Otro test-framework en Scala? ¿cuántos llevamos ya? ¿42?“, “Para nada es una broma, se trata de MUnit, y está desarrollado por el excelente equipo detrás de Metals, Scalameta, Coursier o Scala.js”, “¡Wow! Esa si que es una buena carta de presentación, ¿y qué vamos a ver?“, “Pues veremos todo lo que permite hacer la librería, desde un test sencillo, hasta combinaciones mas complicadas con otras librerías como testcontainers o scalacheck, así como las principales diferencias (y los puntos donde destaca) con otras librerías de testing”, “¡Vale, pues me has convencido! ¡No me lo perderé!”"
	document.spanish = true
  document.header = talks/munit.jpg
  document.date = "2020-12-21"
  document.github.link = "https://github.com/alejandrohdezma/alejandrohdezma/tree/main/modules/munit"
  document.youtube.link = "https://www.youtube.com/watch?v=1Veu6QX6oDE"
%}

@:talk

# MUnit

---

## What is it?

///

Test library for Scala

///

Developed by Scalameta

///

Built on top of JUnit

///

Actionable errors

///

Dependency-free

---

![](https://media.giphy.com/media/XDYIuWmMqeEKmqEfEp/giphy.gif)

///

### Alejandro Hernández

Senior Scala developer

Available at:

- [alejandrohdezma.com](https://alejandrohdezma.com)
- [info@alejandrohdezma.com](mailto:info@alejandrohdezma.com)
- [github.com/alejandrohdezma](https://github.com/alejandrohdezma)
- [twitter.com/alejandrohdezma](https://twitter.com/alejandrohdezma)

---

## Why should I choose MUnit?

///

![](https://i.imgur.com/goYdJhw.png)
*https://scalameta.org/munit/*

///

![](https://i.imgur.com/NaAU2He.png)
*https://scalameta.org/munit/*

///

![](https://i.imgur.com/iosErEv.png)
*https://scalameta.org/munit/*

///

![](https://i.imgur.com/hmL0hAp.png)
*https://scalameta.org/munit/*

///

| Scala Version | JVM | Scala.js (0.6.x) | Scala.js (1.x) | Native (0.4.x) |
| ------------- | :-: | :--------------: | :------------: | :------------: |
| 2.11.x        | ✅  |        ✅        |       ✅       |       ✅       |
| 2.12.x        | ✅  |        ✅        |       ✅       |      n/a       |
| 2.13.x        | ✅  |        ✅        |       ✅       |      n/a       |
| 3.0.0-M2      | ✅  |       n/a        |       ✅       |      n/a       |
| 3.0.0-M1      | ✅  |       n/a        |       ✅       |      n/a       |

---

### How do I use it?

///

```sbt
libraryDependencies +=
    "org.scalameta" %% "munit" % "0.7.19"

testFrameworks +=
  new TestFramework("munit.Framework")
```

---

### How does a test looks like in MUnit?

///

```scala
class MySuite extends munit.FunSuite {

  test("It should work as expected") {
    val obtained = MyService().getTheThing()

    val expected = Right(42)

    assertEquals(obtained, expected)
  }

}
```

///

When searching for failed tests in CI logs...

| Test       | Prefix  |
| ---------- | ------- |
| ✅ Success | `+`     |
| ❌ Failed  | `==> X` |
| ⚠️ Ignored | `==> i` |
| ℹ️ Skipped | `==> s` |

---

![](https://media.giphy.com/media/8JW82ndaYfmNoYAekM/giphy.gif)

---

![](https://media.giphy.com/media/KJ1f5iTl4Oo7u/giphy.gif)

@:@