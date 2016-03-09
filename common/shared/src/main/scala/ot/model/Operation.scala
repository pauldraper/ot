package ot.model

import prickle._
import scala.annotation.tailrec

trait Operation {
  /**
    * Apply this operation to the document
    */
  def apply(document: Document): Document
}

trait ShiftOperation {
  def index: Int

  def shifts: Int

  def shift(x: Int): Operation
}

object Operation {

  implicit val pickler: PicklerPair[Operation] = CompositePickler[Operation]
    .concreteType[Delete].concreteType[Move].concreteType[Insert]

  def apply(operations: Seq[Operation])(document: Document) = operations.foldLeft(document) { (document, operation) =>
    operation(document)
  }

  /**
    * Basically, the OT xform(a,b) function.
    */
  def rebase(server: Seq[Operation], client: Seq[Operation]) = {
    server.foldLeft(client) { (client, operation) =>
      @tailrec
      def f(base: Operation, initial: List[Operation], `final`: List[Operation] = Nil): List[Operation] =
        initial match {
          case operation +: tail => (base, operation) match {
            case (base: ShiftOperation, operation: ShiftOperation) =>
              if (base.index <= operation.index) {
                f(base, tail, operation.shift(base.shifts) +: `final`)
              } else {
                f(base.shift(operation.shifts), tail, operation +: `final`)
              }
            case (base: Move, operation: Delete) if base.from == operation.index =>
              tail reverse_::: (operation.copy(index = base.to) +: `final`)
            case (base: Delete, operation: Move) if base.index == operation.from =>
              f(base.copy(index = operation.to), tail, `final`)
            case (base: Move, operation: ShiftOperation) =>
              if (base.from < operation.index && base.to < operation.index) {
                f(base, tail, operation +: `final`)
              } else if (base.from < operation.index && base.to >= operation.index) {
                f(base.shiftTo(operation.shifts), tail, operation.shift(-1) +: `final`)
              } else if (base.from >= operation.index && base.to < operation.index) {
                f(base.shiftFrom(operation.shifts), tail, operation.shift(1) +: `final`)
              } else {
                f(base.shiftFrom(operation.shifts).shiftTo(operation.shifts), tail, operation +: `final`)
              }
            case (base: ShiftOperation, operation: Move) =>
              if (base.index < operation.from && base.index < operation.to) {
                f(base, tail, operation.shiftFrom(base.shifts).shiftTo(base.shifts) +: `final`)
              } else if (base.index < operation.from && base.index >= operation.to) {
                f(base.shift(1), tail, operation.shiftFrom(base.shifts) +: `final`)
              } else if (base.index >= operation.from && base.index < operation.to) {
                f(base.shift(-1), tail, operation.shiftTo(base.shifts) +: `final`)
              } else {
                f(base, tail, operation +: `final`)
              }
          }
          case _ => `final`
        }
      f(operation, client.toList)
    }
  }

}

case class Insert(index: Int, word: Word) extends Operation with ShiftOperation {
  def apply(document: Document) = document.copy(words = document.words.take(index) ++ Seq(word) ++ document.words.drop(index))

  def shift(x: Int) = copy(index = index + x)

  def shifts = 1
}

case class Move(from: Int, to: Int) extends Operation {
  def apply(document: Document) = {
    if (from < to) {
      document.copy(
        words = document.words.take(from)
          ++ document.words.slice(from + 1, to)
          ++ Seq(document.words(from))
          ++ document.words.drop(to)
      )
    } else if (from > to) {
      document.copy(
        words = document.words.take(to)
          ++ Seq(document.words(from))
          ++ document.words.slice(to, from)
          ++ document.words.drop(from + 1)
      )
    } else {
      document
    }
  }

  def shiftFrom(x: Int) = copy(from = from + x)

  def shiftTo(x: Int) = copy(to = to + x)
}

case class Delete(index: Int) extends Operation with ShiftOperation {
  def apply(document: Document) = document.copy(words = document.words.take(index) ++ document.words.drop(index + 1))

  def shift(x: Int) = copy(index = index + x)

  def shifts = -1
}
