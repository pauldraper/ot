package ot.model

case class OperationBatch(client: String, operations: Seq[Operation], baseVersion: Int)
