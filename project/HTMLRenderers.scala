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
import laika.bundle.ExtensionBundle
import laika.bundle.RenderOverrides
import laika.format.HTML

object HTMLRenderers extends ExtensionBundle {

  val description: String = "Custom HTML render function"

  override def renderOverrides: Seq[RenderOverrides] = Seq(HTML.Overrides {
    case (fmt, SpanLink(content, ExternalTarget(url), title, opt)) =>
      fmt.element(
        "a",
        opt,
        content,
        fmt.optAttributes(
          "href"   -> Some(url),
          "title"  -> title.map(fmt.text),
          "rel"    -> Some("external"),
          "target" -> Some("_blank")
        ): _*
      )
    case (fmt, Figure(img, caption, _, opt)) =>
      fmt.indentedElement(
        "figure",
        opt,
        List(SpanSequence(img), Paragraph(caption, Style.caption))
      )
    case (fmt, cb @ CodeBlock(lang, content, _, opt)) =>
      fmt.rawElement(
        "pre",
        opt,
        fmt.withoutIndentation { innerFmt =>
          innerFmt.element("button", Options(), Seq(IconStyle("far fa-copy"))) +
            innerFmt.element("code", Styles(s"language-$lang"), content)
        }
      )
    case (fmt, Paragraph(caption, Style.caption)) =>
      fmt.indentedElement("figcaption", Options(), caption)
    case (fmt, BlockSequence(content, Id("details"))) =>
      fmt.indentedElement("details", Options(), content)
    case (fmt, Paragraph(summary, Id("summary"))) =>
      fmt.element("summary", Options(), summary)
  })

}
