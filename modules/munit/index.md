---
css: style.css
theme: blood
highlightTheme: vs2015
verticalSeparator: "///"
revealOptions:
  width: 700
  transition: "fade"
  controls: false
---

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

47 Degrees

Senior Scala developer

Available at:

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
