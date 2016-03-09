package ot.html

import scalatags.Text
import scalatags.Text.all._

object DocumentHtml {

  val colorId = "color"
  val contentId = "content"
  val footerId = "footer"
  val newWordsId = "newWords"

  def apply(headContent: Text.Frag) = {
    val footerHeight = 110.px
    html(
      height := 100.pct,
      margin := 0,
      head(headContent),
      body(
        height := 100.pct,
        margin := 0,
        div(
          height := 100.pct,
          div(
            id := contentId,
            paddingLeft := 10.px,
            paddingRight := 10.px,
            paddingTop := 10.px,
            paddingBottom := footerHeight
          )
        ),
        div(
          height := footerHeight,
          position.relative,
          marginTop := s"-$footerHeight",
          background := "#ddd",
          div(
            id := footerId,
            padding := 10.px,
            div(
              display.`inline-block`,
              color := "red",
              id := colorId,
              height := 70.px,
              width := 70.px,
              lineHeight := 70.px,
              fontSize := 80.px,
              fontFamily := "sans-serif",
              textAlign.center,
              verticalAlign.middle
            ),
            span(
              id := newWordsId
            )
          )
        )
      )
    )
  }

}