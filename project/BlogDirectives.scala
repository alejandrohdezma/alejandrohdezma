import laika.directive.DirectiveRegistry

import laika.directive.Blocks
import laika.ast._
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

case object BlogDirectives extends DirectiveRegistry {

  /** Use it in templates as `@:blog` to create an `<ul>` with the list of articles in that folder. */
  val blogDirective = Blocks.create("blog") {
    laika.directive.Blocks.dsl.cursor.map { cursor =>
      val articles = cursor.parent.allDocuments.toList
        .filter(_.path.basename != "README")
        .map { document =>
          for {
            title       <- document.config.get[String]("document.title")
            header      <- document.config.get[String]("document.header")
            description <- document.config.get[String]("document.description")
            date        <- document.config.get[String]("document.date").map(LocalDate.parse)
            locale      <- document.config.get[Boolean]("document.spanish").map(if (_) new Locale("es") else Locale.US)
          } yield Article(document.path, title, header, description, date, locale)
        }
        .mapFilter(_.toOption)
        .sortBy(_.date.toEpochDay())(Ordering.Long.reverse)
        .map { article =>
          BulletListItem(
            List(
              BlockSequence(
                BlockSequence(article.link(Image(AbsoluteInternalTarget(Path(List("images", article.header)))))),
                Header(3, article.link(Text(article.title.toUpperCase()))),
                Paragraph(Text(article.formattedDate, Options(styles = Set("time")))),
                Paragraph(Text(article.description))
              )
            ),
            StringBullet("")
          )
        }

      BulletList(articles, StringBullet(""), Options(Some("blog-posts")))
    }
  }

  val figureDirective = Blocks.create("figure") {
    import Blocks.dsl._

    (attribute(0).as[String].widen, attribute("caption").as[String], attribute("caption-link").as[String]).mapN {
      (src, caption, captionLink) =>
        Figure(Image(ExternalTarget(src)), Seq(SpanLink(Seq(Text(caption)), ExternalTarget(captionLink))), Nil)

    }
  }

  /** Use it in templates as `@:date` to create a text span with the article's localized date. */
  val dateDirective = Templates.create("date") {
    laika.directive.Templates.dsl.cursor
      .map(_.config)
      .map { config =>
        for {
          date   <- config.get[String]("document.date").map(LocalDate.parse)
          locale <- config.get[Boolean]("document.spanish").map(if (_) new Locale("es") else Locale.US)
        } yield TemplateString(date.format(formatter.withLocale(locale)))
      }
      .evalMap(_.left.map(_.message))
  }

  val spanDirectives = Nil

  val blockDirectives = List(blogDirective, figureDirective)

  val templateDirectives = List(dateDirective)

  val linkDirectives = Nil

  /** Represents a blog article */
  final case class Article(
      path: Path,
      title: String,
      header: String,
      description: String,
      date: LocalDate,
      locale: Locale
  ) {

    lazy val formattedDate = date.format(formatter.withLocale(locale))

    def link(span: Span) = SpanLink(Seq(span), InternalTarget(path))

  }

  private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

}
