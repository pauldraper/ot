package ot.model

import org.scalatest.WordSpec

class OperationSpec extends WordSpec {

  private[this] def word(value: String) = Word(value, Color.White)

  private[this] def document(values: String*) = Document(values.map(word))

  "rebase" should {

    "merge insert and delete" in {
      val originalDocument = document("A", "B")

      val serverOperations = Seq(Delete(0))
      val serverDocument = Operation(serverOperations)(originalDocument)
      assert(serverDocument == document("B"))

      val clientOperations = Seq(Insert(1, word("_")))
      val clientDocument = Operation(clientOperations)(originalDocument)
      assert(clientDocument == document("A", "_", "B"))

      val rebasedOperations = Operation.rebase(serverOperations, clientOperations)
      val finalDocument = Operation(rebasedOperations)(serverDocument)
      assert(finalDocument == document("_", "B"))
    }

    "merge insert and insert" in {
      val originalDocument = document("A", "B")

      val serverOperations = Seq(Insert(0, word("1")))
      val serverDocument = Operation(serverOperations)(originalDocument)
      assert(serverDocument == document("1", "A", "B"))

      val clientOperations = Seq(Insert(1, word("2")))
      val clientDocument = Operation(clientOperations)(originalDocument)
      assert(clientDocument == document("A", "2", "B"))

      val rebasedOperations = Operation.rebase(serverOperations, clientOperations)
      val finalDocument = Operation(rebasedOperations)(serverDocument)
      assert(finalDocument == document("1", "A", "2", "B"))
    }

    "merge delete and move" in {
      val originalDocument = document("A", "B", "C")

      val serverOperations = Seq(Delete(0))
      val serverDocument = Operation(serverOperations)(originalDocument)
      assert(serverDocument == document("B", "C"))

      val clientOperations = Seq(Move(0, 2))
      val clientDocument = Operation(clientOperations)(originalDocument)
      assert(clientDocument == document("B", "A", "C"))

      val rebasedOperations = Operation.rebase(serverOperations, clientOperations)
      val finalDocument = Operation(rebasedOperations)(serverDocument)
      assert(finalDocument == document("B", "C"))
    }

  }

}