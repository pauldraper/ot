package ot.html

import scalatags.Text
import scalatags.Text.all._

object DocumentHtml {

  val colorId = "color"
  val contentId = "content"
  val footerId = "footer"
  val newWordsId = "newWords"
  val connectButtonId = "connect"
  val logId = "log"

  def apply(headContent: Text.Frag) = {
    val footerHeight = 90.px
    val sidebarWidth = 300.px
    html(
      height := 100.pct,
      margin := 0,
      head(headContent),
      body(
        height := 100.pct,
        margin := 0,
        div(
          id := contentId,
          paddingLeft := 10.px,
          paddingRight := 10.px,
          paddingTop := 10.px,
          paddingBottom := footerHeight,
          position.absolute,
          left := 0,
          top := 0,
          right := sidebarWidth,
          bottom := footerHeight
        ),
        div(
          background := "#eee",
          height := "100%",
          position.absolute,
          right := 0,
          top := 0,
          bottom := footerHeight,
          width := sidebarWidth,
          borderLeft := "solid 1px black",
          textAlign.center,
          paddingTop := 20.px,
          div(
            border := s"${1.px} solid black",
            borderRadius := 12.px,
            background := "white",
            cursor := "pointer",
            id := connectButtonId,
            display.`inline-block`,
            width := 150.px,
            height := 40.px,
            lineHeight := 40.px,
            fontSize := 18.px,
            "Loading..."
          ),
          div(
            id := logId,
            background := "black",
            position.absolute,
            top := 80.px,
            bottom := 150.px,
            left := 20.px,
            right := 20.px,
            fontSize := 14.px,
            textAlign.left,
            border := "solid 1px black",
            padding := 10.px
          )
        ),
        div(
          height := footerHeight,
          position.absolute,
          borderTop := "solid 1px black",
          bottom := 0,
          left := 0,
          right := 0,
          marginTop := s"-$footerHeight",
          background := "#eee",
          id := footerId,
          padding := 10.px,
          whiteSpace.nowrap,
          div(
            display.`inline-block`,
            color := "red",
            border := "solid 1px black",
            id := colorId,
            height := 70.px,
            width := 70.px,
            lineHeight := 70.px,
            fontSize := 80.px,
            fontFamily := "sans-serif",
            textAlign.center,
            verticalAlign.middle
          ),
          div(
            display.`inline-block`,
            id := newWordsId,
            height := 70.px
          )
        )
      )
    )
  }

}