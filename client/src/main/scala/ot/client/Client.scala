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
      for (i <- 1 to 4) {
        val footer = document.getElementById(DocumentHtml.footerId)
        def wordChoice(): Node = {
          val word = Word(words(Random.nextInt(words.size)), color)
          val node = textToNode(WordHtml(word))
          node.asInstanceOf[raw.HTMLElement].style.background = ""
          node.addEventListener("click", (_: Any) => {
            node.parentNode.replaceChild(wordChoice(), node)
            localOperation(Insert(localState.words.size, word))
          })
          node
        }
        footer.appendChild(wordChoice())
      }

      document.getElementById(DocumentHtml.colorId).asInstanceOf[HTMLElement].style.background = color.hex
    }
  }

  private[this] val localOperations = ArrayBuffer[Operation]()
  private[this] var localState = ModelDocument(Nil)

  private[this] val savedBatches = ArrayBuffer[OperationBatch]()
  private[this] var savedState = localState

  private[this] var saveReady = false

  def connect(): Unit = {
    saveReady = false
    ws = new WebSocket("ws://localhost:9000/echo")
    ws.onmessage = (message: MessageEvent) => {
      val batches = Unpickle[Seq[OperationBatch]].fromString(message.data.asInstanceOf[String]).get
      batches.foreach(remoteBatch)
      saveReady = true
      save()
      refreshDocument()
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

  def localOperation(operation: Operation) = {
    localState = operation(localState)
    localOperations += operation
    save()
    refreshDocument()
  }

  def refreshDocument() = {
    val content = document.getElementById(DocumentHtml.contentId)
    while (content.firstChild != null) {
      content.removeChild(content.firstChild)
    }
    localState.words.foreach { word =>
      content.appendChild(textToNode(WordHtml(word)))
    }
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
