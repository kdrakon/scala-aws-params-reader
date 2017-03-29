package io.policarp.scala.aws.params

object Params {

  sealed abstract class ParamType(val name: String)
  class StringParam extends ParamType("String")
  object StringParam extends StringParam
  class StringListParam extends ParamType("StringList")
  object StringListParam extends StringListParam
  class SecureStringParam extends ParamType("SecureString")
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

  implicit class ParamLikeImplicits[A, B <: ParamType](param: ParamLike[A, B]) {
    def wasSecured: Boolean = {
      param match {
        case _: SecureParam[A] => true
        case _: ParamLike[A, B] => false
      }
    }
  }
}

