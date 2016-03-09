package ot.client

import org.scalajs.dom._
import org.scalajs.dom.raw.HTMLElement
import ot.html.{DocumentHtml, WordHtml}
import ot.model.{Document => ModelDocument, _}
import prickle.{Pickle, Unpickle}
import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js.JSApp
import scala.util.Random

object Client extends JSApp {
  import ot.json.Implicits._

  private[this] val clientId = Random.alphanumeric.take(10).mkString

  private[this] var ws: WebSocket = _
  private[this] var color: Color = _

  private[this] var selectionState = Option.empty[Either[Int, Int]]

  private[this] val colors = Seq(
    Color(255, 10, 10),
    Color(10, 255, 10),
    Color(10, 10, 255),
    Color(255, 255, 10),
    Color(255, 10, 255),
    Color(10, 255, 255)
  )

  def main(): Unit = {
    color = colors(Random.nextInt(colors.size))

    connect()

    window.onload = (_: Event) => {
      val colorElement = document.getElementById(DocumentHtml.colorId).asInstanceOf[HTMLElement]
      colorElement.style.background = color.hex
      colorElement.onclick = (_: Any) => selectionState.foreach { selection =>
        selection.left.foreach { i =>
          selectionState = None
          localOperation(Delete(i))
        }
      }
    }
  }

  private[this] val localOperations = ArrayBuffer[Operation]()
  private[this] var localState = ModelDocument(Nil)

  private[this] val savedBatches = ArrayBuffer[OperationBatch]()
  private[this] var savedState = localState

  private[this] var saveReady = false

  private[this] val newWords = ArrayBuffer[Word]()

  def connect(): Unit = {
    saveReady = false
    ws = new WebSocket("ws://localhost:9000/echo")
    ws.onmessage = (message: MessageEvent) => {
      val batches = Unpickle[Seq[OperationBatch]].fromString(message.data.asInstanceOf[String]).get
      batches.foreach(remoteBatch)
      saveReady = true
      save()
      render()
    }
    ws.onclose = (_: Any) => {
      println("WebSocket closed!")
      connect()
    }
    ws.onopen = (_: Any) => ws.send(Pickle.intoString(savedBatches.size))
  }

  def save() = if (saveReady && localOperations.nonEmpty) {
    ws.send(Pickle.intoString(OperationBatch(clientId, localOperations, savedBatches.size)))
    saveReady = false
  }

  def remoteBatch(batch: OperationBatch) = {
    if (batch.client == clientId) {
      localOperations.remove(0, batch.operations.size)
    }
    savedState = {
      val serverOperations = savedBatches.drop(batch.baseVersion).flatMap(_.operations)
      val clientOperations = batch.operations
      Operation(Operation.rebase(serverOperations, clientOperations))(savedState)
    }

    val newLocalOperations = Operation.rebase(batch.operations, localOperations)
    localOperations.clear()
    localOperations ++= newLocalOperations
    localState = Operation(localOperations)(savedState)

    savedBatches += batch
  }

  def localOperation(operation: Operation): Unit = {
    localState = operation(localState)
    localOperations += operation
    save()
    render()
  }

  def render(): Unit = {
    val content = document.getElementById(DocumentHtml.contentId)
    while (content.firstChild != null) {
      content.removeChild(content.firstChild)
    }
    def insert(index: Int) = selectionState.map { selection =>
      import scalatags.JsDom.all._
      val element = div(
        background := "red",
        display.`inline-block`,
        height := 70.px,
        marginLeft := (-7).px,
        marginRight := (-7).px,
        verticalAlign.middle,
        width := 14.px
      ).render
      element.onclick = (_: Any) => {
        selectionState = None
        localOperation(selection.fold(Move(_, index), { i =>
          val word = newWords(i)
          newWords.remove(i)
          Insert(index, word)
        }))
      }
      element
    }
    localState.words.zipWithIndex.foreach { case (word, index) =>
      insert(index).foreach(content.appendChild)
      content.appendChild {
        val element = textToNode(WordHtml(word)).asInstanceOf[HTMLElement]
        element.onclick = { _: Any =>
          selectionState = Some(Left(index))
          render()
        }
        if (selectionState.contains(Left(index))) {
          element.style.color = "red"
        }
        element
      }
    }
    insert(localState.words.size).foreach(content.appendChild)

    while (newWords.size < 4) {
      newWords += Word(words(Random.nextInt(words.size)), this.color)
    }
    val newWordsElement = document.getElementById(DocumentHtml.newWordsId)
    while (newWordsElement.firstChild != null) {
      newWordsElement.removeChild(newWordsElement.firstChild)
    }
    newWords.zipWithIndex.foreach { case (word, index) =>
      val element = textToNode(WordHtml(word)).asInstanceOf[raw.HTMLElement]
      element.style.background = ""
      if (selectionState.contains(Right(index))) {
        element.style.color = "red"
      }
      element.onclick = (_: Any) => {
        selectionState = Some(Right(index))
        render()
      }
      newWordsElement.appendChild(element)
    }

    val color = document.getElementById(DocumentHtml.colorId)
    color.textContent = if (selectionState.exists(_.isLeft)) "X" else ""
  }

  def textToNode(text: scalatags.Text.TypedTag[String]) = {
    import scalatags.JsDom.all._
    div(raw(text.toString)).render.childNodes.item(0)
  }

  private[this] val words = Seq(
    "a",
    "bannana",
    "the",
    ".",
    "ate",
    "happy",
    "big",
    "to"
  )
}
