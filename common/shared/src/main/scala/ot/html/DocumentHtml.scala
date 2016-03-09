package ot.html

import scalatags.Text
import scalatags.Text.all._

object DocumentHtml {

  val colorId = "color"
  val contentId = "content"
  val footerId = "footer"

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
              id := colorId,
              height := 70.px,
              width := 70.px,
              verticalAlign.middle
            )
          )
        )
      )
    )
  }

}