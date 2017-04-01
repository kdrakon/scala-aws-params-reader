package io.policarp.scala.aws.params.reader

import io.policarp.scala.aws.params.Params.ParamResult.{Invalid, InvalidParam, ParamResult, Valid}
import io.policarp.scala.aws.params.reader.ListWriter.ListSeparator
import io.policarp.scala.aws.params.reader.ValueWriters.ValueWriter

case class ListWriter[A](valueWriter: ValueWriter[A], listSeparator: ListSeparator) extends ValueWriter[Seq[A]] {
  override def as(name: String, param: String): ParamResult[Seq[A]] = {

    lazy val init: ParamResult[Seq[A]] = Valid(Seq[A]())
    lazy val fail: ParamResult[Seq[A]] = Invalid(InvalidParam[Seq[A]](name))
    val split = param.split(listSeparator.separator).toList

    split match {
      case head :: Nil if head.isEmpty => init
      case params =>
        params.map(p => valueWriter.as(name, p)).foldLeft(init)((result, param) => {
          // Does not work in pre-2.12 (Either right-biased)
          // for {
          //   seq <- result
          //   p <- param.fold(_ => fail, r => Valid[Seq[A]](Seq(r)))
          // } yield {
          //   seq ++ p
          // }

          // Keep while supporting 2.11
          result match {
            case invalid: Invalid[Seq[A]] => invalid
            case Right(seq) => param.fold(_ => fail, r => Valid[Seq[A]](seq :+ r))
          }
        })
    }
  }
}

object ListWriter {

  sealed case class ListSeparator(separator: String)
  object Comma extends ListSeparator(",")
  object Semicolon extends ListSeparator(";")
  object Space extends ListSeparator(" ")

}