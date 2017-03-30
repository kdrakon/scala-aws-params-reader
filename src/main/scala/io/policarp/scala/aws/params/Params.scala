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
  case class SecureParam[A](override val name: String, value: A, encrypted: Boolean) extends ParamLike[A, SecureStringParam]
  case class InvalidParam[A](name: String)

  object ParamResult {
    type ParamResult[A, T <: ParamLike[A, _ <: ParamType]] = Either[InvalidParam[A], T]
    type Valid[A, T <: ParamLike[A, _ <: ParamType]] = Right[InvalidParam[A], T]
    type Invalid[A, T <: ParamLike[A, _ <: ParamType]] = Left[InvalidParam[A], T]

    def Valid[A, T <: ParamLike[A, _ <: ParamType]](p: T): Valid[A, T] = {
      Right[InvalidParam[A], T](p)
    }
    def Invalid[A, T <: ParamLike[A, _ <: ParamType]](p: InvalidParam[A]): Invalid[A, T] = {
      Left[InvalidParam[A], T](p)
    }
  }

  implicit class ParamLikeImplicits[A, B <: ParamType](param: ParamLike[A, B]) {

    import ParamResult._

    def wasSecured: Boolean = {
      param match {
        case p: SecureParam[A] if !p.encrypted => true
        case _: ParamLike[A, B] => false
      }
    }

    def asParam: ParamResult[A, Param[A]] = {
      param match {
        case p: Param[A] => Valid(p)
        case _ => Invalid(InvalidParam[A](param.name))
      }
    }

    def asParamList: ParamResult[A, ParamList[A]] = {
      param match {
        case p: ParamList[A] => Valid(p)
        case _ => Invalid(InvalidParam[A](param.name))
      }
    }

    def asSecureParam: ParamResult[A, SecureParam[A]] = {
      param match {
        case p: SecureParam[A] => Valid(p)
        case _ => Invalid(InvalidParam[A](param.name))
      }
    }
  }
}

