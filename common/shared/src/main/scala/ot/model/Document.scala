package ot.model

case class Document(words: Seq[Word])

case class Word(value: String, color: Color)

case class Color(red: Int, green: Int, blue: Int) {
  def hex = f"#$red%02X$green%02X$blue%02X"
}

object Color {
  def White = apply(255, 255, 255)
}
