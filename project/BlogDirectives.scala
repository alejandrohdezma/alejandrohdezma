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
          } yield Article(document.path, title, header, description, date)
        }
        .mapFilter(_.toOption)
        .sortBy(_.date.toEpochDay())(Ordering.Long.reverse)
        .map { article =>
          BulletListItem(
            List(
              BlockSequence(
                BlockSequence(article.link(Image(AbsoluteInternalTarget(Path(List("images", article.header)))))),
                Header(3, article.link(Text(article.title.toUpperCase()))),
                Paragraph(Text(article.date.format(locale), Styles("time"))),
                Paragraph(Text(article.description))
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
    laika.directive.Templates.dsl.cursor
      .map(_.target.path.withoutSuffix.toString())
      .map(_.replace("/README", ""))
      .map(TemplateString(_))
  }

  val alternateUrlDirective = Templates.create("alternateUrl") {
    laika.directive.Templates.dsl.cursor.map { document =>
      val spanish = document.config.get[Boolean]("document.spanish").getOrElse(false)

      val path = document.config
        .get[String]("document.translation.path")
        .fold(_ => document.path, document.path.withBasename(_))
        .withoutSuffix
        .toString()
        .replace("/README", "/")
        .stripSuffix("/")

      if (path.isEmpty() || path == "/es")
        TemplateString(if (spanish) "/" else s"/es")
      else
        TemplateString(if (spanish) path.stripPrefix("/es") else s"/es$path")
    }
  }

  val localeDirective = Templates.create("locale") {
    laika.directive.Templates.dsl.cursor.map { document =>
      val spanish = document.config.get[Boolean]("document.spanish").getOrElse(false)

      TemplateString(if (spanish) "es_ES" else "en_US")
    }
  }

  val alternateLocaleDirective = Templates.create("alternateLocale") {
    laika.directive.Templates.dsl.cursor.map { document =>
      val spanish = document.config.get[Boolean]("document.spanish").getOrElse(false)

      TemplateString(if (spanish) "en_US" else "es_ES")
    }
  }

  val spanDirectives = Nil

  val blockDirectives = List(blogDirective, talksDirective, figureDirective, detailsDirective, talkDirective)

  val templateDirectives =
    List(dateDirective, urlDirective, alternateUrlDirective, localeDirective, alternateLocaleDirective)

  val linkDirectives = Nil

  /** Represents a blog article */
  final case class Article(
      path: Path,
      title: String,
      header: String,
      description: String,
      date: LocalDate
  ) {

    def link(span: Span) = SpanLink(Seq(span), InternalTarget(path.withoutSuffix))

  }

  private def getLocale(document: DocumentCursor) = {
    val locale = if (isSpanish(document)) new Locale("es") else Locale.US

    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)
  }

  private def isSpanish(document: DocumentCursor) = document.path.toString().startsWith("/es")

}
