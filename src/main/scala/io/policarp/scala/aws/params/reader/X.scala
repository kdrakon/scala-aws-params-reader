package io.policarp.scala.aws.params.reader

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClient
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest
import io.policarp.scala.aws.params.reader.ValueWriters.ValueWriter
import io.policarp.scala.aws.params.reader.X.{ ParamType, _ }

import scala.collection.JavaConverters._

object X {

  sealed abstract class ParamType(val name: String)
  class StringParam extends ParamType("String")
  object StringParam extends StringParam
  class StringListParam extends ParamType("String list")
  object StringListParam extends StringListParam
  class SecureStringParam extends ParamType("Secure string")
  object SecureStringParam extends SecureStringParam
  class UnknownParam extends ParamType("")

  sealed trait ParamLike[A, B <: ParamType] { val name: String }
  case class Param[A](override val name: String, value: A) extends ParamLike[A, StringParam]
  case class ParamList[A](override val name: String, value: Seq[A]) extends ParamLike[A, StringListParam]
  case class SecureParam[A](override val name: String, value: A) extends ParamLike[A, SecureStringParam]
  case class InvalidParam[A](name: String)

  object ParamResult {
    type ParamResult[A] = Either[InvalidParam[A], ParamLike[A, _ <: ParamType]]
    type Valid[A, B <: ParamType] = Right[InvalidParam[A], ParamLike[A, B]]
    type Invalid[A, B <: ParamType] = Left[InvalidParam[A], ParamLike[A, B]]

    def Valid[A, B <: ParamType](p: ParamLike[A, B]): Valid[A, B] = {
      Right[InvalidParam[A], ParamLike[A, B]](p)
    }
    def Invalid[A, B <: ParamType](p: InvalidParam[A]): Invalid[A, B] = {
      Left[InvalidParam[A], ParamLike[A, B]](p)
    }
  }

  val StringListParamSeparator = ","

  implicit class ParamLikeImplicits[A, B <: ParamType](param: ParamLike[A, B]) {
    def wasSecured: Boolean = {
      param match {
        case _: SecureParam[A] => true
        case _: ParamLike[A, B] => false
      }
    }

    def isValid: Boolean = {
      param match {
        case _: InvalidParam[A] => false
        case _: ParamLike[A, B] => true
      }
    }
  }
}

trait ParamReader {

  import X.ParamResult._

  val client: AWSSimpleSystemsManagementClient

  def read[A](name: String)(implicit valueWriter: ValueWriter[A]): ParamResult[A] = ???

  def read[A](names: Seq[String])(implicit valueWriter: ValueWriter[A]): Map[String, ParamResult[A]] = {

    val result = client.getParameters {
      new GetParametersRequest()
        .withNames(names: _*)
        .withWithDecryption(true)
    }

    val valid: Map[String, Right[InvalidParam[A], _ <: ParamLike[A, _ <: ParamType]]] = result.getParameters.asScala.map(p => {
      p.getType match {
        case StringParam.name => p.getName -> Valid(Param[A](p.getName, valueWriter.as(p.getValue)))
        case StringListParam.name => p.getName -> Valid(ParamList[A](p.getName, p.getValue.split(StringListParamSeparator).map(valueWriter.as)))
        case SecureStringParam.name => p.getName -> Valid(SecureParam[A](p.getName, valueWriter.as(p.getValue)))
      }
    }).toMap

    val invalid: Map[String, Left[InvalidParam[A], _ <: ParamLike[A, _ <: ParamType]]] =
      result.getInvalidParameters.asScala.map(p => p -> Invalid(InvalidParam[A](p))).toMap

    valid ++ invalid
  }
}

object ValueWriters {

  trait ValueWriter[A] {
    def as(param: String): A
  }

  implicit object StringValueWriter extends ValueWriter[String] {
    override def as(param: String): String = param
  }

}

object Test extends App {

  import ValueWriters._
  import X._

  val x: ParamReader = ???

  val y = x.read[String](Seq("", ""))
  y.get("").foreach({
    case Left(x) => x.name
    case Right(a: Param[String]) => ???
    case Right(a: ParamList[String]) => ???
    case Right(a: SecureParam[String]) => ???
  })

  val u: ParamType = ???

}
