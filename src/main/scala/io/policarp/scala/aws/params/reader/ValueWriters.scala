package io.policarp.scala.aws.params.reader

import io.policarp.scala.aws.params.Params.ParamResult._

import scala.concurrent.duration.Duration

object ValueWriters {

  trait ValueWriter[A] {
    def as(name: String, param: String): ParamResult[A]
  }

  case class ListWriter[A](valueWriter: ValueWriter[A], stringListSeparator: String) extends ValueWriter[Seq[A]] {
    override def as(name: String, param: String): ParamResult[Seq[A]] = {

      lazy val init: ParamResult[Seq[A]] = Valid(Seq[A]())
      lazy val fail: ParamResult[Seq[A]] = Invalid(InvalidParam[Seq[A]](name))
      val split = param.split(stringListSeparator).toSeq

      split match {
        case head :: Nil if head == param => fail // split didn't work
        case params =>
          params.map(p => valueWriter.as(name, p)).foldLeft(init)((result, param) => {
            for {
              seq <- result
              p <- param.fold(_ => fail, r => Valid[Seq[A]](Seq(r)))
            } yield {
              seq ++ p
            }
          })
      }
    }
  }

  implicit object StringValueWriter extends ValueWriter[String] {
    override def as(name: String, param: String): ParamResult[String] = Valid[String](param)
  }

  implicit object BooleanValueWriter extends ValueWriter[Boolean] {
    override def as(name: String, param: String): ParamResult[Boolean] = {
      try {
        Valid[Boolean](param.toBoolean)
      } catch {
        case _: java.lang.IllegalArgumentException => Invalid(InvalidParam[Boolean](name))
      }
    }
  }

  implicit object IntValueWriter extends ValueWriter[Int] {
    override def as(name: String, param: String): ParamResult[Int] = {
      try {
        Valid[Int](param.toInt)
      } catch {
        case _: java.lang.NumberFormatException => Invalid(InvalidParam[Int](name))
      }
    }
  }

  implicit object LongValueWriter extends ValueWriter[Long] {
    override def as(name: String, param: String): ParamResult[Long] = {
      try {
        Valid[Long](param.toLong)
      } catch {
        case _: java.lang.NumberFormatException => Invalid(InvalidParam[Long](name))
      }
    }
  }

  implicit object DurationWriter extends ValueWriter[Duration] {
    override def as(name: String, param: String): ParamResult[Duration] = {
      try {
        Valid[Duration](Duration.create(param))
      } catch {
        case _: java.lang.NumberFormatException => Invalid(InvalidParam[Duration](name))
      }
    }
  }

}
