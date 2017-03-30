package io.policarp.scala.aws.params.reader

import scala.concurrent.duration.Duration

object ValueWriters {

  trait ValueWriter[A] {
    def as(param: String): A
  }

  implicit object StringValueWriter extends ValueWriter[String] {
    override def as(param: String): String = param
  }

  implicit object BooleanValueWriter extends ValueWriter[Boolean] {
    override def as(param: String): Boolean = param.toBoolean
  }

  implicit object IntValueWriter extends ValueWriter[Int] {
    override def as(param: String): Int = param.toInt
  }

  implicit object LongValueWriter extends ValueWriter[Long] {
    override def as(param: String): Long = param.toLong
  }

  implicit object DurationWriter extends ValueWriter[Duration] {
    override def as(param: String): Duration = Duration.create(param)
  }

}
