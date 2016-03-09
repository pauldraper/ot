package ot.server

import ot.html.DocumentHtml
import ot.model.{Document, Operation, OperationBatch}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.{Concurrent, Iteratee}
import play.api.mvc._
import play.twirl.api.{Html, HtmlFormat}
import prickle.{Pickle, Unpickle}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.implicitConversions
import scalatags.Text
import scalatags.Text.all._

class Server extends Controller {
  import ot.json.Implicits._

  implicit def htmlToTagFragment(html: HtmlFormat.Appendable): Text.Frag = raw(html.body)

  implicit def tagFragmentToHtml(fragment: Text.Frag): HtmlFormat.Appendable = Html(fragment.toString)

  def apply = Action {
    Ok[Html](DocumentHtml(playscalajs.html.scripts("client")))
  }

  def version(version: Int): Action[RequestHeader] = ???

  private[this] val clients = mutable.Set[Concurrent.Channel[String]]()

  private[this] var state = Document(Nil)
  private[this] val batches = ArrayBuffer[OperationBatch]()

  def socket = WebSocket.using[String] { request =>
    val (out, channel) = Concurrent.broadcast[String]
    val in = Iteratee.head[String].map { string =>
      val version = Unpickle[Int].fromString(string.get).get
      channel.push(Pickle.intoString(batches.drop(version).toSeq))
      clients += channel
    }.flatMap { _ =>
      Iteratee.foreach[String] { string =>
        println(string)
        val batch = Unpickle[OperationBatch].fromString(string).get
        state = {
          val serverOperations = batches.drop(batch.baseVersion).flatMap(_.operations)
          val clientOperations = batch.operations
          Operation(Operation.rebase(serverOperations, clientOperations))(state)
        }
        batches += batch
        val outMessage = Pickle.intoString(Seq(batch))
        clients.foreach(_.push(outMessage))
      }.map { _ =>
        clients -= channel
      }
    }
    (in, out)
  }

}
