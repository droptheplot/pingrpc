package pingrpc.ui

import org.fxmisc.richtext.CodeArea

import scala.annotation.tailrec

object JsonHighlighter {
  private case class Span(style: String, start: Int, end: Option[Int])

  def highlight(codeArea: CodeArea): Unit =
    generateSpans(codeArea.getText.zipWithIndex.toList).foreach { span =>
      codeArea.setStyleClass(span.start, span.end.getOrElse(span.start), span.style)
    }

  @tailrec
  private def generateSpans(charsWithIndex: List[(Char, Int)], spans: List[Span] = List.empty, spanOpt: Option[Span] = None): List[Span] =
    charsWithIndex match {
      case (char, index) :: rest if char == '"' =>
        spanOpt match {
          case Some(span) => generateSpans(rest, spans :+ span.copy(end = Some(index + 1)), None)
          case None => generateSpans(rest, spans, Some(Span("warning", index, None)))
        }
      case _ :: rest => generateSpans(rest, spans, spanOpt)
      case Nil => spans
    }
}
