package io.policarp.scala.aws.params.reader

object ValueWriters {

  trait ValueWriter[A] {
    def as(param: String): A
  }

  implicit object StringValueWriter extends ValueWriter[String] {
    override def as(param: String): String = param
  }

}
