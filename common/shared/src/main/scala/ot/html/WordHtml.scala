package ot.html

import ot.model
import scalatags.Text.all._

object WordHtml {

  def apply(word: model.Word) = {
    val height_ = 70.px
    div(
      background := word.color.hex,
      border := s"${1.px} solid black",
      borderRadius := 12.px,
      `class` := "word",
      display.`inline-block`,
      fontSize := 40.px,
      height := height_,
      lineHeight := height_,
      margin := 10.px,
      textAlign.center,
      width := 200.px,
      word.value,
      verticalAlign.middle
    )
  }

}
