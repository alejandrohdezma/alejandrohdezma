import laika.directive.DirectiveRegistry

import laika.directive.Blocks
import laika.ast._
import cats.data.NonEmptySet
import cats.syntax.all._
import laika.rst.ast._
import laika.render.FOFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import laika.directive.Templates
import laika.directive.Spans
import laika.sbt.LaikaPlugin.autoImport._
import org.apache.batik.svggen.ImageCacher.External
import laika.config.Config

case object BlogDirectives extends DirectiveRegistry {

  /** Use it in templates as `@:blog` to create an `<ul>` with the list of articles in that folder. */
  val blogDirective = Blocks.create("blog") {
    laika.directive.Blocks.dsl.cursor.map { cursor =>
      val locale = getLocale(cursor)

      val articles = cursor.parent.allDocuments.toList
        .filter(_.path.basename != "README")
        .map { document =>
          for {
            title       <- document.config.get[String]("document.title")
            header      <- document.config.get[String]("document.header")
            description <- document.config.get[String]("document.description")
            date        <- document.config.get[String]("document.date").map(LocalDate.parse)
          } yield (document.path, title, header, description, date)
        }
        .mapFilter(_.toOption)
        .sortBy(_._5.toEpochDay())(Ordering.Long.reverse)
        .map { case (path, title, header, description, date) =>
          val link = (span: Span) => SpanLink(Seq(span), InternalTarget(path.withoutSuffix))
          BulletListItem(
            List(
              BlockSequence(
                BlockSequence(link(Image(AbsoluteInternalTarget(Path(List("images", header)))))),
                Header(3, link(Text(title.toUpperCase()))),
                Paragraph(Text(date.format(locale), Styles("time"))),
                Paragraph(Text(description))
              )
            ),
            StringBullet("")
          )
        }

      BulletList(articles, StringBullet(""), Id("blog-posts"))
    }
  }

  /** Use it in templates as `@:talks` to create an `<ul>` with the list of talks in that folder. */
  val talksDirective = Blocks.create("talks") {
    laika.directive.Blocks.dsl.cursor.map { cursor =>
      val locale = getLocale(cursor)

      val talks = cursor.parent.allDocuments.toList
        .filter(_.path.basename != "README")
        .mapFilter { document =>
          for {
            title       <- document.config.get[String]("document.title").toOption
            header      <- document.config.get[String]("document.header").toOption
            description <- document.config.get[String]("document.description").toOption
            date        <- document.config.get[String]("document.date").map(LocalDate.parse).toOption
            github      <- document.config.get[String]("document.github.link").toOption
            youtube     <- document.config.get[String]("document.youtube.link").toOption
          } yield (document.path, title, header, description, date, github, youtube)
        }
        .sortBy(_._5.toEpochDay())(Ordering.Long.reverse)
        .map { case (path, title, header, description, date, github, youtube) =>
          val youtubeLink = SpanLink.external(youtube)

          BulletListItem(
            List(
              BlockSequence(
                BlockSequence(youtubeLink(Image(AbsoluteInternalTarget(Path(List("images", header)))))),
                Header(3, youtubeLink(Text(title.toUpperCase()))),
                Paragraph(Text(date.format(locale), Styles("time"))),
                Paragraph(Text(description)),
                BlockSequence(
                  SpanLink.external(github)(IconStyle("fab fa-github"), Text(" GitHub")),
                  SpanLink.internal(path.withoutSuffix)(IconStyle("fab fa-slideshare"), Text(" Slides"))
                )
              )
            ),
            StringBullet("")
          )
        }

      if (talks.size > 0)
        BulletList(talks, StringBullet(""), Id("talks"))
      else
        Paragraph(Seq(Text("No talks available in this language")))
    }
  }

  val figureDirective = Blocks.create("figure") {
    import Blocks.dsl._

    (attribute(0).as[String].widen, attribute("caption").as[String], attribute("caption-link").as[String]).mapN {
      (src, caption, captionLink) =>
        Figure(Image(ExternalTarget(src)), Seq(SpanLink(Seq(Text(caption)), ExternalTarget(captionLink))), Nil)
    }
  }

  val detailsDirective = Blocks.create("details") {
    import Blocks.dsl._

    parsedBody.evalMap {
      case Paragraph(summary, _) :: content if content.size >= 1 =>
        Right(BlockSequence(Paragraph(summary, Id("summary")) +: content, Id("details")))
      case _ => Left("A details block must contains two inner blocks: the summary (header) and the content (body).")
    }
  }

  val talkDirective = Blocks.create("talk") {
    Blocks.dsl.rawBody.map(RawContent(NonEmptySet.of("html"), _))
  }

  /** Use it in templates as `@:date` to create a text span with the article's localized date. */
  val dateDirective = Templates.create("date") {
    laika.directive.Templates.dsl.cursor.map { document =>
      document.config
        .get[String]("document.date")
        .map(LocalDate.parse(_).format(getLocale(document)))
        .map(TemplateString(_))
    }.evalMap(_.left.map(_.message))
  }

  val urlDirective = Templates.create("url") {
    import laika.directive.Templates.dsl._

    (allAttributes, cursor).mapN { case (attributes, document) =>
      (attributes.get[Boolean]("alternate").getOrElse(false), isSpanish(document), document.path, document.config)
    }.map {
      case (false, _, path, _) => TemplateString(path.withoutSuffix.toString().replace("/README", ""))
      case (true, spanish, path, config) =>
        val uri = config
          .get[String]("document.translation.path")
          .fold(_ => path, path.withBasename(_))
          .withoutSuffix
          .toString()
          .replace("/README", "/")

        TemplateString(if (spanish) uri.stripPrefix("/es") else s"/es$uri")
    }
  }

  val localeDirective = Templates.create("locale") {
    import laika.directive.Templates.dsl._

    (allAttributes, cursor).mapN { case (attributes, document) =>
      (
        attributes.get[Boolean]("short").getOrElse(false),
        attributes.get[Boolean]("alternate").getOrElse(false),
        isSpanish(document)
      )
    }.map {
      case (true, true, true)    => "us"
      case (false, false, true)  => "es-ES"
      case (false, true, true)   => "en-US"
      case (true, false, true)   => "es"
      case (true, true, false)   => "es"
      case (false, false, false) => "en-US"
      case (false, true, false)  => "es-ES"
      case (true, false, false)  => "en"
    }.map(TemplateString(_))
  }

  val k8sDirective = Templates.create("k8s") {
    import laika.directive.Templates.dsl._

    (attribute(0).as[String], cursor).mapN { (key, document) =>
      val translations = Map(
        "/blog/"                                      -> "/es/blog/",
        "Go back"                                     -> "Volver atrás",
        "Ver versión en español"                      -> "See english version",
        "../images/"                                  -> "../../images/",
        "Did you enjoy the article?"                  -> "¿Te ha gustado el artículo?",
        "If you want"                                 -> "Si quieres",
        "you can buy me a coffee"                     -> "puedes invitarme a un café",
        "Did you find any misprint or error? Please," -> "¿Has encontrado una errata?",
        "send me a PR!"                               -> "¡Envíame una PR!",
        "/"                                           -> "/es/",
        "/cv"                                         -> "/es/cv",
        "/talks/"                                     -> "/es/talks/",
        "/blog/"                                      -> "/es/blog/",
        "About me"                                    -> "Sobre mí",
        "My CV"                                       -> "Mi CV",
        "My talks"                                    -> "Mis charlas",
        "Blog"                                        -> "Blog",
        "header_us.webp"                              -> "header_es.webp"
      )

      val text = if (document.path.toString().startsWith("/es")) translations(key) else key

      TemplateString(text)
    }
  }

  val spanDirectives = Nil

  val blockDirectives = List(blogDirective, talksDirective, figureDirective, detailsDirective, talkDirective)

  val templateDirectives = List(dateDirective, urlDirective, localeDirective, k8sDirective)

  val linkDirectives = Nil

  private def getLocale(document: DocumentCursor) = {
    val locale = if (isSpanish(document)) new Locale("es") else Locale.US

    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)
  }

  private def isSpanish(document: DocumentCursor) = document.path.toString().startsWith("/es")

}
