package ot.model

case class OperationBatch(client: String, color: Color, operations: Seq[Operation], baseVersion: Int)
